import socket
import ssl
import time

class UnicastConnection:
    STRICT_SSL_VALIDATION = True
    BUFFER_SIZE=8000

    def __init__(self, sock=None):
        if sock is None:
            self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        else:
            self.sock = sock
        self.is_connected = False
        self.is_tls = False
        self.ssock = None
        self.ca_path = None
        self.ssl_info = None

    def set_ca_path(self, ca_path):
        self.ca_path = ca_path

    def connect(self, host, port, tls):
        print("[UnicastConnection::connect]")
        self.is_tls = tls
        if self.is_tls:
            print("[UnicastConnection::connect] Attempting SSL/TLS connection")
            context = ssl.create_default_context()
            if UnicastConnection.STRICT_SSL_VALIDATION:
                if self.ca_path:
                    print("[UnicastConnection::connect] loading CA.CRT")
                    context.load_verify_locations(self.ca_path)
            else:
                context.check_hostname = False
                context.verify_mode = ssl.CERT_NONE

            self.ssock = context.wrap_socket(self.sock, server_hostname=host)
            self.ssock.connect((host,port))
            certinfo = self.ssock.getpeercert()
            if certinfo:
                cert_subject = dict(x[0] for x in certinfo['subject'])
                cert_issuer = dict(x[0] for x in certinfo['issuer'])
                print(cert_subject['commonName'])
                print(cert_issuer['commonName'])
                self.ssl_info = f"Certificate Informatinon:\nCN={cert_subject['commonName']}\nIssuer={cert_issuer['commonName']}\nTLS version: {self.ssock.version()}"
            self.is_connected = True
        else:
            print("[UnicastConnection::connect] Attemping unencypted connection")
            self.sock.connect((host, port))
            self.is_connected = True
    def read_line(self):
        print("[UnicastConnection::read_line] begin")
        if self.is_tls:
            line = self.ssock.recv(1000)
        else:
            line = self.sock.recv(1000)
        message = line.decode('utf-8')
        print(f"[UnicastConnection::read_line] Received: '{message}'")
        return message

    def send_message(self, message):
        print(f"[UnicastConnection::send_message] sending message '{message}'")
        if self.is_tls:
            self.ssock.sendall(message.encode('utf-8'))
        else:
            self.sock.sendall(message.encode('utf-8'))

    def get_certificate_info(self):
        return self.ssl_info

    def close(self):
        self.sock.close()
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.is_connected = False
        self.is_tls = False
        self.ssock = None
        self.ca_path = None
        self.ssl_info = None
