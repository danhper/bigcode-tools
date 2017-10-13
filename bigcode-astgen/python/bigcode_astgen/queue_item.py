class ProcessedFileItem:
    def __init__(self, filename, ast, options):
        self.filename = filename
        self.ast = ast
        self.min_nodes = options.get("min_nodes", 0)
        self.max_nodes = options.get("max_nodes", 10000000)

    @property
    def success(self):
        if self.min_nodes <= len(self.ast) <= self.max_nodes:
            return True
        return False

    @property
    def reason(self):
        if len(self.ast) < self.min_nodes:
            return "too few nodes"
        if len(self.ast) > self.max_nodes:
            return "too many nodes"


class FailedFileItem:
    def __init__(self, filename, raw_reason):
        self.filename = filename
        self.raw_reason = raw_reason
        self.success = False

    @property
    def reason(self):
        return str(self.raw_reason)
