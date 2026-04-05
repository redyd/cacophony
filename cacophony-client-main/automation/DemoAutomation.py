import json


class DemoAutomation:
    def __init__(self, window, script_path):
        self.window = window
        self.script_path = script_path

    def apply_window_options(self):
        with open(self.script_path, encoding="utf-8") as handle:
            config = json.load(handle)

        self.window.set_window_title(config.get("title", ""))
        self.window.set_geometry(config.get("geometry", ""))
        self.window.set_silent_dialogs(bool(config.get("silentDialogs", False)))

    def start(self):
        with open(self.script_path, encoding="utf-8") as handle:
            config = json.load(handle)

        for action in config.get("actions", []):
            delay_ms = int(float(action.get("at", 0)) * 1000)
            self.window.schedule(delay_ms, self._build_callback(action))

    def _build_callback(self, action):
        def callback():
            kind = action.get("type")
            if kind == "connect":
                if action.get("onboarding", False):
                    self.window.onboarding()
                else:
                    self.window.authenticate()
            elif kind == "subscribe":
                self.window.message.set(action["target"])
                self.window.subscribe()
            elif kind == "send":
                self.window.message.set(action["message"])
                self.window.send_message()
            elif kind == "note":
                self.window.add_message(f"[demo] {action['message']}", False)
            elif kind == "close":
                self.window.quit()
            else:
                self.window.add_message(f"[demo] unknown action ignored: {kind}", False)

        return callback
