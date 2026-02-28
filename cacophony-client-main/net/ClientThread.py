import threading
from model.Protocol import *

class ClientThread(threading.Thread):
    def __init__(self, app_controller, connection, parser):
        super(ClientThread, self).__init__()
        self.app_controller = app_controller
        self.connection = connection
        self.parser = parser
        self.stop = False
        self.exiting = False

    def stop_loop(self):
        self.stop = True

    def on_quit(self):
        self.exiting = True

    def run(self) -> None:
        while not self.stop:
            line = self.connection.read_line()
            if not(line and line.strip()):
                self.stop = True
            else:
                message_id = self.parser.parse(line, False)
                if message_id == Protocol.PARSE_MSG:
                    tokens = self.parser.parse_MSG(line)
                    self.app_controller.new_message(tokens[0], tokens[1])
                elif message_id == Protocol.PARSE_OK and self.exiting == True :
                    self.connection.close()
                    self.stop = True

        self.connection.close()