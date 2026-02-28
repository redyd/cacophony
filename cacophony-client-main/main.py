from gui.MainWindow import *
from controller.MainWindowController import *
from controller.AppController import *
from model.Protocol import *
import sys
import os.path

if __name__ == '__main__':
    parser = Protocol()
    app_controller = AppController(parser)
    window_controller = MainWindowController(parser, app_controller)
    window = MainWindow(window_controller)
    if os.path.isfile('./ca.crt'):
        print("[main] Found ca.crt")
        app_controller.ssl_ca_path('./ca.crt')
    if len(sys.argv) > 1:
        window.host.set(sys.argv[1])
    if len(sys.argv) > 2:
        window.port.set(int(sys.argv[2]))
    if len(sys.argv) > 3:
        window.login.set(sys.argv[3])
    if len(sys.argv) > 4:
        window.passw.set(sys.argv[4])
    if len(sys.argv) > 5:
        window.tls.set(sys.argv[5] == 'tls')
    window.start_main_loop()
