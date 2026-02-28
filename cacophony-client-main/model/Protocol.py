import re

class Protocol:
    RX_DIGIT = r"[0-9]"
    RX_LETTER = r"[a-zA-Z]"
    RX_DIGIT_LETTER = r"" + RX_DIGIT + "|" + RX_LETTER
    RX_CHARACTER = r"[\x20-\xff]"
    RX_NEWLINE = r"(\\x0d\\x0a){0,1}"
    RX_SPECIALCHAR = r"[\x21-\x2f]|[\x3a-\x40]|[\x5B-\x60]"
    RX_SP = r" "
    RX_DOMAIN = r"((" + RX_DIGIT_LETTER + "|\.){5,200})"
    RX_COUNTER = r"(" + RX_DIGIT + "{2})"
    RX_RANDOM_STRING = r"((" + RX_DIGIT_LETTER + "|" + RX_SPECIALCHAR + "){22})"
    # RX_PASSCHAR = r"[\x22-\xff]"
    RX_USER = r"((" + RX_DIGIT_LETTER +"){5,20})"
    RX_USER_AT_DOMAIN = r"(" + RX_USER + "@" + RX_DOMAIN + ")"
    RX_MESSAGE = r"((" + RX_CHARACTER + "){1,250})"
    RX_INFORMATION_MESSAGE = r"((" + RX_CHARACTER + "){0,200})"
    RX_WELCOME = r"WELCOME" + RX_SP + RX_DOMAIN + RX_SP + RX_RANDOM_STRING + RX_NEWLINE
    RX_CHALLENGE = r"CHALLENGE" + RX_SP + RX_COUNTER + RX_SP + RX_RANDOM_STRING + RX_NEWLINE
    RX_MSG = r"MSG" + RX_SP + RX_USER_AT_DOMAIN + RX_SP + RX_MESSAGE + RX_NEWLINE
    RX_OK = r"OK" + RX_INFORMATION_MESSAGE + RX_NEWLINE
    RX_ERROR = r"ERROR" + RX_INFORMATION_MESSAGE + RX_NEWLINE 
    ALL_MESSAGES = [RX_WELCOME, RX_CHALLENGE, RX_OK, RX_ERROR, RX_MSG]
    PARSE_UNKNOWN = -1
    PARSE_WELCOME = 0
    PARSE_CHALLENGE = 1
    PARSE_OK = 2
    PARSE_ERROR = 3
    PARSE_MSG = 4
    USER_MSG = "USER <user>\r\n"
    ONBOARDING_MSG = "ONBOARDING <user> <salt_length> <bcrypt_hash>\r\n"
    RESPONSE_MSG = "RESPONSE <sha3hexstring>\r\n"
    SEND_MSG = "SEND <message>\r\n"
    SUBSCRIBE_MSG = "SUBSCRIBE <subscriber>\r\n"
    LEAVE_MSG = "LEAVE\r\n"

    def build_USER(self, user):
        return Protocol.USER_MSG.replace("<user>", user)

    def build_ONBOARDING(self, user, length, hash):
        return Protocol.ONBOARDING_MSG.replace("<user>", user).replace("<salt_length>", f"{length}").replace("<bcrypt_hash>", hash)

    def build_RESPONSE(self, sha3hex):
        return Protocol.RESPONSE_MSG.replace("<sha3hexstring>", sha3hex)

    def build_SEND(self, message):
        return Protocol.SEND_MSG.replace("<message>", message)

    def build_SUBSCRIBE(self, subscriber):
        return Protocol.SUBSCRIBE_MSG.replace("<subscriber>", subscriber)

    def build_LEAVE(self):
        return Protocol.LEAVE_MSG
    
    def parse_WELCOME(self, message):
        if(self.parse(message, False) == Protocol.PARSE_WELCOME):
            return self.get_elements_from_regex(message, Protocol.RX_WELCOME, [1,3])
    
    def parse_CHALLENGE(self, message):
        if(self.parse(message, False) == Protocol.PARSE_CHALLENGE):
            return self.get_elements_from_regex(message, Protocol.RX_CHALLENGE, [1,2])

    def parse_MSG(self, message):
        if self.parse(message, False) == Protocol.PARSE_MSG:
            return self.get_elements_from_regex(message, Protocol.RX_MSG, [1,6])

    def get_elements_from_regex(self, message, rx, elements):
        result = []
        group_list = re.search(rx, message)
        if(group_list != None):
            for val in elements:
                result.append(group_list.group(val))
            return result
        else: 
            return None


    def parse(self, line, debug_enabled):
        for i in range(len(Protocol.ALL_MESSAGES)):
            if(re.match(Protocol.ALL_MESSAGES[i], line) != None):
                if(debug_enabled):
                    print(f"OK REGEXP: {Protocol.ALL_MESSAGES[i]}")
                return i
            else:
                if(debug_enabled):
                    print(f"KO REGEXP {Protocol.ALL_MESSAGES[i]}")
        return Protocol.PARSE_UNKNOWN

if __name__ == '__main__':
    protocol = Protocol()
    line = input("> ")
    while(line != ""):
        val = protocol.parse(line, True)
        v = protocol.get_elements_from_regex(line, Protocol.RX_MSG, [1,6])
        print(f"Val = {v}")
        line = input("> ")