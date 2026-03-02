class MainWindowController:
    def __init__(self, parser, app_controller):
        self.parser = parser
        self.app_controller = app_controller
        self.app_controller.set_window_controller(self)
        self.window = None

    def on_quit(self):
        self.app_controller.on_quit()
        pass

    def on_onboarding(self, host, port, login, passw, tls):
        print("[MainWindowController::on_onboarding] calling AppController::on_authenticate")
        self.app_controller.on_authenticate(host, port, login, passw, tls, True)

    def on_authenticate(self, host, port, login, passw, tls):
        self.app_controller.on_authenticate(host, port, login, passw, tls)

    def on_send(self, message):
        self.app_controller.on_send(message)

    def on_subscribe(self, follow):
        self.app_controller.on_subscribe(follow)

    def add_message(self, message, is_colored):
        self.window.add_message(message, is_colored)

    def show_message(self, message, is_error):
        self.window.show_message(message, is_error)

    def switch_connected_mode(self):
        self.window.connected_mode()

    def register_window(self, window):
        self.window = window
