from net.UnicastConnection import *
from model.Protocol import *
from tools.HashManager import *
from net.ClientThread import *
import traceback
import os.path

class AppController:
    def __init__(self, parser):
        self.parser = parser
        self.connection = UnicastConnection()
        self.hashmanager = HashManager()
        self.client_thread = None
        self.window_controller = None
        self.current_salt = None
        self.current_domain = None

    def ssl_ca_path(self, ca_path):
        self.connection.set_ca_path(ca_path)

    def set_window_controller(self, window_controller):
        self.window_controller = window_controller


    def on_authenticate(self, host, port, login, passw, tls, onboarding=False):
        print("[AppController::on_authenticate]")
        try:
            if not self.connection.is_connected:
                print("[AppController::on_authenticate] connecting ... calling UnicastConnection::connect")
                self.connect(host, port, tls)
            line = self.connection.read_line()
            print(f"[AppController::on_authenticate] Received : '{line}'")
            message_id = self.parser.parse(line, False)
            if message_id == Protocol.PARSE_WELCOME:
                tokens = self.parser.parse_WELCOME(line)
                if onboarding == True:
                    if self.manage_onboarding(login, passw) == True:
                        self.window_controller.switch_connected_mode()
                        self.client_thread = ClientThread(self, self.connection, self.parser)
                        self.client_thread.start()
                    else:
                        self.show_message("Onboarding failed : check if username is available", True)
                        self.connection.close()
                else:
                    if self.manage_authenticate(tokens[0], tokens[1], login, passw) == True:
                        self.window_controller.switch_connected_mode()
                        self.client_thread = ClientThread(self, self.connection, self.parser)
                        self.client_thread.start()
                    else:
                        self.show_message("Authentication failed : check your credentials", True)
                        self.connection.close()
        except Exception as ex:
            print(ex)
            traceback.print_exc()

    def manage_authenticate(self, domain, challenge, login, passw):
        print("[AppController::manage_authenticate]")
        try:
            self.connection.send_message(self.parser.build_USER(login))
            line = self.connection.read_line()
            print(f"[AppController::manage_authenticate] Received : '{line}'")
            message_id = self.parser.parse(line, False)
            if message_id == Protocol.PARSE_CHALLENGE:
                tokens = self.parser.parse_CHALLENGE(line)
                sha3hex = self.hashmanager.compute_sha3hex(passw, tokens[1], int(tokens[0]), challenge)
                self.connection.send_message(self.parser.build_RESPONSE(sha3hex))
                return self.is_ok()
            else:
                return False
        except Exception as ex:
            print(ex)
            traceback.print_exc()
            return False

    def manage_onboarding(self, login, passw):
        print("[AppController::manage_onboarding]")
        try:
            bhash = self.hashmanager.compute_bcrypt2(passw)
            self.connection.send_message(self.parser.build_ONBOARDING(login, self.hashmanager.salt_length(), bhash))
            return self.is_ok()

        except Exception as ex:
            print(ex)
            traceback.print_exc()
            return False
    
    def is_ok(self):
        try:
            line = self.connection.read_line()
            message_id = self.parser.parse(line, False)
            return (message_id == self.parser.PARSE_OK)

        except Exception as ex:
            print(ex)
            traceback.print_exc()
            return False

    def on_quit(self):
        if self.connection.is_connected:
            if self.client_thread != None:
                self.client_thread.on_quit()
                self.client_thread.stop_loop()
            self.connection.send_message(self.parser.build_LEAVE())
            
    def on_send(self, message):
        if self.connection.is_connected:
            self.connection.send_message(self.parser.build_SEND(message))
            self.new_message(None, message)
        else:
            self.show_message("Not connected !", True)
            
    def on_subscribe(self, subscriber):
        if self.connection.is_connected:
            self.connection.send_message(self.parser.build_SUBSCRIBE(subscriber))
        else:
            self.show_message("Not connected !", True)

    def new_message(self, sender, message):
        if sender != None:
            self.window_controller.add_message(f"[{sender}] {message}", True)
        else:
            self.window_controller.add_message(f"[me] {message}", False)

    def show_message(self,message, is_error):
        self.window_controller.show_message(message, is_error)

    def connect(self, host, port, tls):
        try:
            self.connection.connect(host, port, tls)
            if tls and UnicastConnection.STRICT_SSL_VALIDATION:
                self.show_message(self.connection.ssl_info, False)
            elif tls and not UnicastConnection.STRICT_SSL_VALIDATION:
                self.show_message("SSL/TLS enabled, but certificate NOT checked !", False)
            else:
                self.show_message("No SSL connection", False)

        except Exception as ex:
            self.show_message(f"Error during connection !\n{ex}", True)
            self.connection = UnicastConnection()
