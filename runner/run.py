#!/usr/bin/env python3
from __future__ import annotations

import argparse
import hashlib
import json
import shutil
import signal
import socket
import ssl
import subprocess
import sys
import threading
import time
from collections import deque
from dataclasses import dataclass
from datetime import datetime
from pathlib import Path
try:
    import bcrypt
except ImportError as exc:  # pragma: no cover - import guard
    raise SystemExit(
        "Missing Python dependency 'bcrypt'. "
        "Install it or run with the project's Python environment."
    ) from exc


ROOT = Path(__file__).resolve().parent.parent
RUNNER_DIR = ROOT / "runner"
NODE_DIR = ROOT / "node"
SPEAKER_DIR = ROOT / "speaker"
CLIENT_DIR = ROOT / "cacophony-client-main"
NODE_SCRIPT = ROOT / "node" / "build" / "install" / "node" / "bin" / "node"
SPEAKER_SCRIPT = ROOT / "speaker" / "build" / "install" / "speaker" / "bin" / "speaker"

READY_TIMEOUT_SECONDS = 45.0
PROCESS_STOP_TIMEOUT_SECONDS = 5.0


class RunnerError(RuntimeError):
    pass


@dataclass(frozen=True)
class ClientConfig:
    name: str
    host: str
    port: int
    login: str
    password: str
    tls: bool


@dataclass(frozen=True)
class ReceivedMessage:
    sender: str
    content: str


class ManagedProcess:
    def __init__(self, name: str, command: list[str], cwd: Path, log_path: Path) -> None:
        self.name = name
        self.command = command
        self.cwd = cwd
        self.log_path = log_path
        self.process: subprocess.Popen[str] | None = None
        self._lines: deque[str] = deque(maxlen=2000)
        self._lock = threading.Lock()
        self._reader_thread: threading.Thread | None = None

    def start(self) -> None:
        self.log_path.parent.mkdir(parents=True, exist_ok=True)
        log_file = self.log_path.open("w", encoding="utf-8")
        self.process = subprocess.Popen(
            self.command,
            cwd=self.cwd,
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            stdin=subprocess.DEVNULL,
            text=True,
            bufsize=1,
            universal_newlines=True,
        )

        def pump() -> None:
            assert self.process is not None
            assert self.process.stdout is not None
            with log_file:
                for line in self.process.stdout:
                    stamped = line.rstrip("\n")
                    log_file.write(stamped + "\n")
                    log_file.flush()
                    with self._lock:
                        self._lines.append(stamped)

        self._reader_thread = threading.Thread(target=pump, name=f"log-{self.name}", daemon=True)
        self._reader_thread.start()

    def wait_for_log(self, needle: str, timeout: float) -> None:
        deadline = time.time() + timeout
        while time.time() < deadline:
            if self.process is None:
                raise RunnerError(f"{self.name} has not been started")
            if self.process.poll() is not None:
                raise RunnerError(
                    f"{self.name} exited early with code {self.process.returncode}. "
                    f"See {self.log_path}"
                )
            with self._lock:
                if any(needle in line for line in self._lines):
                    return
            time.sleep(0.2)
        raise RunnerError(
            f"Timed out waiting for '{needle}' in {self.name} logs. "
            f"See {self.log_path}"
        )

    def stop(self) -> None:
        if self.process is None or self.process.poll() is not None:
            return
        self.process.terminate()
        try:
            self.process.wait(timeout=PROCESS_STOP_TIMEOUT_SECONDS)
        except subprocess.TimeoutExpired:
            self.process.kill()
            self.process.wait(timeout=PROCESS_STOP_TIMEOUT_SECONDS)


class HeadlessClient:
    def __init__(self, config: ClientConfig) -> None:
        self.config = config
        self.socket: socket.socket | None = None
        self.reader = None
        self._messages: list[ReceivedMessage] = []
        self._lines: list[str] = []
        self._condition = threading.Condition()
        self._thread: threading.Thread | None = None
        self._connected = False

    def connect(self) -> None:
        if self._connected:
            return
        try:
            raw_socket = socket.create_connection((self.config.host, self.config.port), timeout=10)
        except socket.gaierror as exc:
            raise RunnerError(
                f"Could not resolve {self.config.host}. "
                f"Check your hosts/DNS configuration for TLS mode."
            ) from exc
        except OSError as exc:
            raise RunnerError(
                f"Could not connect to {self.config.host}:{self.config.port}: {exc}"
            ) from exc

        sock = raw_socket
        if self.config.tls:
            try:
                context = ssl.create_default_context(cafile=str(CLIENT_DIR / "ca.crt"))
                sock = context.wrap_socket(raw_socket, server_hostname=self.config.host)
            except ssl.SSLError as exc:
                raw_socket.close()
                raise RunnerError(
                    f"TLS handshake failed for {self.config.name} on {self.config.host}:{self.config.port}. "
                    f"Check the hosts mapping and certificate configuration: {exc}"
                ) from exc
        self.socket = sock
        self.reader = sock.makefile("r", encoding="utf-8", newline="\r\n")

        welcome = self._read_line(timeout=10)
        domain, challenge = self._parse_welcome(welcome)
        self._send_line(f"USER {self.config.login}\r\n")

        challenge_line = self._read_line(timeout=10)
        rounds, salt = self._parse_challenge(challenge_line)
        response = self._compute_response(self.config.password, salt, rounds, challenge)
        self._send_line(f"RESPONSE {response}\r\n")

        ok_line = self._read_line(timeout=10)
        if not ok_line.startswith("OK"):
            raise RunnerError(
                f"Authentication failed for {self.config.name}: received '{ok_line.rstrip()}'"
            )

        self._connected = True
        self._thread = threading.Thread(target=self._listen, name=f"client-{self.config.name}", daemon=True)
        self._thread.start()
        print(f"[client] {self.config.name} authenticated on {domain}:{self.config.port}")

    def subscribe(self, target: str) -> None:
        self._ensure_connected()
        self._send_line(f"SUBSCRIBE {target}\r\n")
        print(f"[client] {self.config.name} -> SUBSCRIBE {target}")

    def send_message(self, message: str) -> None:
        self._ensure_connected()
        self._send_line(f"SEND {message}\r\n")
        print(f"[client] {self.config.name} -> SEND {message}")

    def expect_message(self, sender: str, contains: str, timeout: float) -> ReceivedMessage:
        deadline = time.time() + timeout
        with self._condition:
            while True:
                for msg in self._messages:
                    if msg.sender == sender and contains in msg.content:
                        return msg
                remaining = deadline - time.time()
                if remaining <= 0:
                    raise RunnerError(
                        f"{self.config.name} did not receive expected message "
                        f"from {sender!r} containing {contains!r}"
                    )
                self._condition.wait(timeout=remaining)

    def close(self) -> None:
        if not self._connected:
            return
        try:
            self._send_line("LEAVE\r\n")
        except OSError:
            pass
        if self.socket is not None:
            try:
                self.socket.close()
            except OSError:
                pass
        self._connected = False

    def _listen(self) -> None:
        while self._connected and self.reader is not None:
            try:
                line = self.reader.readline()
            except OSError:
                return
            if not line:
                return
            normalized = line.lstrip("\r\n")
            if not normalized.strip():
                continue
            with self._condition:
                self._lines.append(normalized.rstrip("\r\n"))
                if normalized.startswith("MSG "):
                    sender, content = self._parse_msg(normalized)
                    message = ReceivedMessage(sender=sender, content=content)
                    self._messages.append(message)
                    print(f"[recv] {self.config.name} <= [{sender}] {content}")
                self._condition.notify_all()

    def _read_line(self, timeout: float) -> str:
        if self.socket is None or self.reader is None:
            raise RunnerError(f"{self.config.name} is not connected")
        self.socket.settimeout(timeout)
        try:
            while True:
                line = self.reader.readline()
                if not line:
                    break
                normalized = line.lstrip("\r\n")
                if normalized.strip():
                    line = normalized
                    break
        finally:
            self.socket.settimeout(None)
        if not line:
            raise RunnerError(f"Connection closed while waiting for data for {self.config.name}")
        return line

    def _send_line(self, line: str) -> None:
        if self.socket is None:
            raise RunnerError(f"{self.config.name} is not connected")
        self.socket.sendall(line.encode("utf-8"))

    def _ensure_connected(self) -> None:
        if not self._connected:
            raise RunnerError(f"{self.config.name} is not connected")

    @staticmethod
    def _parse_welcome(line: str) -> tuple[str, str]:
        if not line.startswith("WELCOME "):
            raise RunnerError(f"Expected WELCOME, got {line.rstrip()!r}")
        parts = line.rstrip("\r\n").split(" ")
        if len(parts) != 3:
            raise RunnerError(f"Malformed WELCOME message: {line.rstrip()!r}")
        return parts[1], parts[2]

    @staticmethod
    def _parse_challenge(line: str) -> tuple[int, str]:
        if not line.startswith("CHALLENGE "):
            raise RunnerError(f"Expected CHALLENGE, got {line.rstrip()!r}")
        parts = line.rstrip("\r\n").split(" ")
        if len(parts) != 3:
            raise RunnerError(f"Malformed CHALLENGE message: {line.rstrip()!r}")
        return int(parts[1]), parts[2]

    @staticmethod
    def _parse_msg(line: str) -> tuple[str, str]:
        prefix, sender, content = line.rstrip("\r\n").split(" ", 2)
        if prefix != "MSG":
            raise RunnerError(f"Malformed MSG message: {line.rstrip()!r}")
        return sender, content

    @staticmethod
    def _compute_response(password: str, salt: str, rounds: int, challenge: str) -> str:
        bcrypt_prefix = f"$2b${rounds}${salt}".encode("utf-8")
        bcrypt_hash = bcrypt.hashpw(password.encode("utf-8"), bcrypt_prefix).decode("utf-8")
        return hashlib.sha3_256(f"{challenge}{bcrypt_hash}".encode("utf-8")).hexdigest()


class RuntimeEnvironment:
    def __init__(self, runtime_dir: Path) -> None:
        self.runtime_dir = runtime_dir
        self.logs_dir = runtime_dir / "logs"
        self.data_dir = runtime_dir / "data"
        self.config_dir = runtime_dir / "configs"
        self.demo_dir = runtime_dir / "demo"
        self.speaker_home = runtime_dir / "speaker-home"

        self.logs_dir.mkdir(parents=True, exist_ok=True)
        self.data_dir.mkdir(parents=True, exist_ok=True)
        self.config_dir.mkdir(parents=True, exist_ok=True)
        self.demo_dir.mkdir(parents=True, exist_ok=True)
        (self.speaker_home / "src" / "main" / "resources").mkdir(parents=True, exist_ok=True)

    def prepare(self, interface: str, scenario_name: str) -> None:
        self._write_data_files(scenario_name)
        self._write_node_configs(interface)
        self._write_speaker_config(interface)

    def _write_data_files(self, scenario_name: str) -> None:
        datasets = self._build_dataset(scenario_name)
        for file_name, payload in datasets.items():
            (self.data_dir / file_name).write_text(
                json.dumps(payload, indent=2) + "\n",
                encoding="utf-8",
            )

    def _build_dataset(self, scenario_name: str) -> dict[str, dict[str, Any]]:
        users = {
            "user.json": {
                "users": [
                    {
                        "login": "bobby",
                        "bcryptHash": "dXqQgfuhGCvhq4PBJwp/c18Qh.rLnmO",
                        "bcryptRound": 14,
                        "bcryptSalt": "G6mDHT73UQG2lRiCew3hqe",
                        "subscribers": []
                    }
                ]
            },
            "user2.json": {
                "users": [
                    {
                        "login": "louis",
                        "bcryptHash": "WQRGW9GuAk0.Dil3.plVKh3WekVRD1u",
                        "bcryptRound": 14,
                        "bcryptSalt": "lv84Vvaa/Bnl3zfvMJy99e",
                        "subscribers": []
                    }
                ]
            },
            "user3.json": {
                "users": [
                    {
                        "login": "louise",
                        "bcryptHash": "Y9LSb/yPEGquXDu5S3y47B53xFHCKye",
                        "bcryptRound": 14,
                        "bcryptSalt": "4M31aNmyo6Xt0re8E73m0u",
                        "subscribers": []
                    }
                ]
            },
        }

        clean_tags = {
            "tag.json": {"tags": []},
            "tag2.json": {"tags": []},
            "tag3.json": {"tags": []},
        }

        prepared_tags = {
            "tag.json": {
                "tags": [
                    {
                        "tag": "#cacophony",
                        "domain": "node1.swilabus.xyz",
                        "subscribers": [
                            "bobby@node1.swilabus.xyz",
                            "louise@node3.swilabus.xyz"
                        ]
                    }
                ]
            },
            "tag2.json": {"tags": []},
            "tag3.json": {
                "tags": [
                    {
                        "tag": "#cacophony",
                        "domain": "node1.swilabus.xyz",
                        "subscribers": [
                            "louise@node3.swilabus.xyz"
                        ]
                    }
                ]
            },
        }

        prepared_users = {
            "user.json": {
                "users": [
                    {
                        "login": "bobby",
                        "bcryptHash": "dXqQgfuhGCvhq4PBJwp/c18Qh.rLnmO",
                        "bcryptRound": 14,
                        "bcryptSalt": "G6mDHT73UQG2lRiCew3hqe",
                        "subscribers": [
                            "louis@node2.swilabus.xyz"
                        ]
                    }
                ]
            },
            "user2.json": users["user2.json"],
            "user3.json": users["user3.json"],
        }

        if scenario_name in {"prepared_demo", "gui_demo"}:
            return {
                **prepared_users,
                **prepared_tags,
            }

        if scenario_name in {"setup_demo", "tag_edge_cases"}:
            return {
                **users,
                **clean_tags,
            }

        return {
            **users,
            **clean_tags,
        }

    def _write_node_configs(self, interface: str) -> None:
        source_dir = NODE_DIR / "src" / "main" / "resources"
        for source_name, target_name in (
            ("node-config.json", "node-config.json"),
            ("node-config2.json", "node-config2.json"),
            ("node-config3.json", "node-config3.json"),
        ):
            data = json.loads((source_dir / source_name).read_text(encoding="utf-8"))
            data["networkInterface"] = interface
            data["tls"] = True
            (self.config_dir / target_name).write_text(
                json.dumps(data, indent=2) + "\n",
                encoding="utf-8",
            )

    def _write_speaker_config(self, interface: str) -> None:
        source = SPEAKER_DIR / "src" / "main" / "resources" / "speaker-config.json"
        data = json.loads(source.read_text(encoding="utf-8"))
        data["networkInterface"] = interface
        target = self.speaker_home / "src" / "main" / "resources" / "speaker-config.json"
        target.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")


class ScenarioRunner:
    def __init__(self, runtime: RuntimeEnvironment, scenario_path: Path, action_delay: float) -> None:
        self.runtime = runtime
        self.scenario_path = scenario_path
        self.action_delay = action_delay
        self.processes: list[ManagedProcess] = []
        self.clients: dict[str, HeadlessClient] = {}

    def build(self, skip_build: bool) -> None:
        if skip_build and NODE_SCRIPT.exists() and SPEAKER_SCRIPT.exists():
            return
        print("[build] running Gradle installDist for node and speaker")
        subprocess.run(
            ["./gradlew", ":node:installDist", ":speaker:installDist"],
            cwd=ROOT,
            check=True,
        )

    def start_infra(self) -> None:
        speaker = ManagedProcess(
            name="speaker",
            command=[str(SPEAKER_SCRIPT)],
            cwd=self.runtime.speaker_home,
            log_path=self.runtime.logs_dir / "speaker.log",
        )
        speaker.start()
        speaker.wait_for_log("Speaker started", timeout=10)
        speaker.wait_for_log("Listening for UDP packets", timeout=10)
        self.processes.append(speaker)
        print(f"[proc] speaker started, logs: {speaker.log_path}")

        for index, args in enumerate(
            (
                [
                    str(self.runtime.config_dir / "node-config.json"),
                    str(self.runtime.data_dir / "user.json"),
                    str(self.runtime.data_dir / "tag.json"),
                ],
                [
                    str(self.runtime.config_dir / "node-config2.json"),
                    str(self.runtime.data_dir / "user2.json"),
                    str(self.runtime.data_dir / "tag2.json"),
                ],
                [
                    str(self.runtime.config_dir / "node-config3.json"),
                    str(self.runtime.data_dir / "user3.json"),
                    str(self.runtime.data_dir / "tag3.json"),
                ],
            ),
            start=1,
        ):
            process = ManagedProcess(
                name=f"node{index}",
                command=[str(NODE_SCRIPT), *args],
                cwd=NODE_DIR,
                log_path=self.runtime.logs_dir / f"node{index}.log",
            )
            process.start()
            process.wait_for_log("Node started", timeout=10)
            self.processes.append(process)
            print(f"[proc] node{index} started, logs: {process.log_path}")

        for process in self.processes:
            if process.name.startswith("node"):
                process.wait_for_log("Speaker connected from:", timeout=READY_TIMEOUT_SECONDS)
                print(f"[proc] {process.name} linked to speaker")

    def execute(self) -> None:
        scenario = json.loads(self.scenario_path.read_text(encoding="utf-8"))
        for client_data in scenario["clients"]:
            config = ClientConfig(
                name=client_data["name"],
                host=client_data["host"],
                port=int(client_data["port"]),
                login=client_data["login"],
                password=client_data["password"],
                tls=bool(client_data.get("tls", False)),
            )
            self.clients[config.name] = HeadlessClient(config)

        print(f"[scenario] {scenario.get('name', self.scenario_path.name)}")
        for action in scenario["actions"]:
            action_type = action["type"]
            if action_type == "connect":
                self.clients[action["client"]].connect()
            elif action_type == "subscribe":
                self.clients[action["client"]].subscribe(action["target"])
            elif action_type == "send":
                self.clients[action["client"]].send_message(action["message"])
            elif action_type == "sleep":
                seconds = float(action["seconds"])
                print(f"[scenario] sleep {seconds:.1f}s")
                time.sleep(seconds)
            elif action_type == "note":
                print(f"[scenario] {action['message']}")
            elif action_type == "expect_msg":
                msg = self.clients[action["client"]].expect_message(
                    sender=action["from"],
                    contains=action["contains"],
                    timeout=float(action.get("timeout", 5)),
                )
                print(f"[assert] {action['client']} received '{msg.content}'")
            elif action_type == "expect_no_msg":
                self._expect_no_message(
                    client=self.clients[action["client"]],
                    sender=action["from"],
                    contains=action["contains"],
                    timeout=float(action.get("timeout", 3)),
                )
                print(f"[assert] {action['client']} did not receive '{action['contains']}'")
            else:
                raise RunnerError(f"Unknown action type: {action_type}")
            if self.action_delay > 0 and action_type != "sleep":
                print(f"[scenario] action delay {self.action_delay:.1f}s")
                time.sleep(self.action_delay)

    def execute_gui_demo(self) -> None:
        scenario = json.loads(self.scenario_path.read_text(encoding="utf-8"))
        client_map = {client["name"]: client for client in scenario["clients"]}
        scripts = self._build_demo_scripts(scenario)

        for client_name, client_data in client_map.items():
            script_path = scripts[client_name]
            command = [
                sys.executable,
                "main.py",
                client_data["host"],
                str(client_data["port"]),
                client_data["login"],
                client_data["password"],
            ]
            if client_data.get("tls", False):
                command.append("tls")
            command.extend(["--demo-script", str(script_path)])
            process = ManagedProcess(
                name=f"client-{client_name}",
                command=command,
                cwd=CLIENT_DIR,
                log_path=self.runtime.logs_dir / f"client-{client_name}.log",
            )
            process.start()
            self.processes.append(process)
            print(f"[proc] client-{client_name} started, logs: {process.log_path}")

        total_duration = max(
            (
                action["at"]
                for action_list in scripts.values()
                for action in json.loads(action_list.read_text(encoding="utf-8"))["actions"]
            ),
            default=0,
        )
        time.sleep(total_duration + 3)
        print("[runner] gui demo finished; clients stay open. Press Ctrl+C to stop the demo.")
        while True:
            for process in self.processes:
                if process.process is not None and process.process.poll() not in (None, 0):
                    raise RunnerError(
                        f"{process.name} exited unexpectedly with code {process.process.returncode}. "
                        f"See {process.log_path}"
                    )
            time.sleep(1)

    def _build_demo_scripts(self, scenario: dict[str, Any]) -> dict[str, Path]:
        absolute_time = 0.0
        per_client_actions: dict[str, list[dict[str, Any]]] = {
            client["name"]: [] for client in scenario["clients"]
        }
        client_meta = {client["name"]: client for client in scenario["clients"]}

        for action in scenario["actions"]:
            action_type = action["type"]
            if action_type == "sleep":
                absolute_time += float(action["seconds"])
                continue
            if action_type == "note" and action.get("client") is None:
                for client_name in per_client_actions:
                    per_client_actions[client_name].append(
                        {
                            "type": "note",
                            "at": absolute_time,
                            "message": action["message"],
                        }
                    )
                continue
            client_name = action.get("client")
            if client_name not in per_client_actions:
                raise RunnerError(f"Unknown client in scenario: {client_name}")
            if action_type not in {"connect", "subscribe", "send", "note", "close"}:
                continue
            payload = {"type": action_type, "at": absolute_time}
            if "target" in action:
                payload["target"] = action["target"]
            if "message" in action:
                payload["message"] = action["message"]
            per_client_actions[client_name].append(payload)
            absolute_time += self.action_delay

        script_paths: dict[str, Path] = {}
        for client_name, actions in per_client_actions.items():
            meta = client_meta[client_name]
            script = {
                "title": meta.get("title", client_name),
                "geometry": meta.get("geometry", ""),
                "silentDialogs": True,
                "actions": actions,
            }
            script_path = self.runtime.demo_dir / f"{client_name}.json"
            script_path.write_text(json.dumps(script, indent=2) + "\n", encoding="utf-8")
            script_paths[client_name] = script_path
        return script_paths

    def _expect_no_message(
        self,
        client: HeadlessClient,
        sender: str,
        contains: str,
        timeout: float,
    ) -> None:
        deadline = time.time() + timeout
        starting_count = self._count_messages(client, sender, contains)
        while time.time() < deadline:
            current_count = self._count_messages(client, sender, contains)
            if current_count > starting_count:
                raise RunnerError(
                    f"{client.config.name} received an unexpected message "
                    f"from {sender!r} containing {contains!r}"
                )
            time.sleep(0.2)

    @staticmethod
    def _count_messages(client: HeadlessClient, sender: str, contains: str) -> int:
        with client._condition:
            return sum(
                1
                for message in client._messages
                if message.sender == sender and contains in message.content
            )

    def close(self) -> None:
        for client in self.clients.values():
            client.close()
        for process in reversed(self.processes):
            process.stop()


def choose_network_interface() -> str:
    try:
        names = [name for _, name in socket.if_nameindex()]
    except PermissionError:
        return "lo"
    for preferred in ("lo", "lo0"):
        if preferred in names:
            return preferred
    for name in names:
        if name.startswith("lo"):
            return name
    if names:
        return names[0]
    raise RunnerError("No network interface found on this machine")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Run local Cacophony integration scenarios.")
    parser.add_argument(
        "--mode",
        choices=("headless", "gui-demo"),
        default="headless",
        help="Execution mode: headless assertions or visible Tkinter demo.",
    )
    parser.add_argument(
        "--scenario",
        default=str(RUNNER_DIR / "scenarios" / "prepared_demo.json"),
        help="Path to the scenario JSON file.",
    )
    parser.add_argument(
        "--skip-build",
        action="store_true",
        help="Do not run Gradle installDist if launch scripts already exist.",
    )
    parser.add_argument(
        "--keep-runtime",
        action="store_true",
        help="Keep the generated runtime directory instead of deleting it on success.",
    )
    parser.add_argument(
        "--action-delay",
        type=float,
        default=0.0,
        help="Extra delay in seconds added after each non-sleep action.",
    )
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    timestamp = datetime.now().strftime("%Y%m%d-%H%M%S")
    runtime_dir = RUNNER_DIR / ".runtime" / timestamp
    scenario_path = Path(args.scenario).resolve()
    interface = choose_network_interface()

    print(f"[runner] runtime directory: {runtime_dir}")
    print(f"[runner] network interface: {interface}")

    runtime = RuntimeEnvironment(runtime_dir)
    runtime.prepare(interface, scenario_path.stem)

    runner = ScenarioRunner(runtime=runtime, scenario_path=scenario_path, action_delay=max(0.0, args.action_delay))
    try:
        runner.build(skip_build=args.skip_build)
        runner.start_infra()
        if args.mode == "gui-demo":
            runner.execute_gui_demo()
        else:
            runner.execute()
            print("[runner] scenario completed successfully")
        return 0
    except KeyboardInterrupt:
        print("[runner] interrupted")
        return 130
    except (RunnerError, subprocess.CalledProcessError) as exc:
        print(f"[runner] failure: {exc}", file=sys.stderr)
        print(f"[runner] logs available in: {runtime.logs_dir}", file=sys.stderr)
        return 1
    finally:
        runner.close()
        if args.keep_runtime:
            print(f"[runner] runtime kept at: {runtime_dir}")
        elif runtime_dir.exists():
            shutil.rmtree(runtime_dir, ignore_errors=True)


if __name__ == "__main__":
    signal.signal(signal.SIGINT, signal.default_int_handler)
    raise SystemExit(main())
