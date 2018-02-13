class Token:
    def __init__(self, token_type: str, value: str = None):
        self.type = token_type
        self.value = value

    def __eq__(self, other):
        if not isinstance(other, Token):
            return False
        return (self.type, self.value) == (other.type, other.value)

    def __hash__(self):
        return hash((self.type, self.value))

    def __repr__(self):
        if self.value:
            return "Token(type='{0}', value='{1}')".format(self.type, self.value)
        else:
            return "Token(type='{0}')".format(self.type)
