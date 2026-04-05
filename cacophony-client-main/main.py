from gui.MainWindow import *
from controller.MainWindowController import *
from controller.AppController import *
from model.Protocol import *
from automation.DemoAutomation import *
import sys
import os.path
import argparse

if __name__ == '__main__':
    cli = argparse.ArgumentParser()
    cli.add_argument("host", nargs="?")
    cli.add_argument("port", nargs="?")
    cli.add_argument("login", nargs="?")
    cli.add_argument("password", nargs="?")
    cli.add_argument("tls", nargs="?")
    cli.add_argument("--demo-script")
    args = cli.parse_args()

    parser = Protocol()
    app_controller = AppController(parser)
    window_controller = MainWindowController(parser, app_controller)
    window = MainWindow(window_controller)
    if os.path.isfile('./ca.crt'):
        print("[main] Found ca.crt")
        app_controller.ssl_ca_path('./ca.crt')

    if args.host:
        window.host.set(args.host)
    if args.port:
        window.port.set(int(args.port))
    if args.login:
        window.login.set(args.login)
    if args.password:
        window.passw.set(args.password)
    if args.tls:
        window.tls.set(args.tls == 'tls')

    if args.demo_script:
        automation = DemoAutomation(window, args.demo_script)
        automation.apply_window_options()
        automation.start()

    window.start_main_loop()
