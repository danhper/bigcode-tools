from os import path
import json
from unittest.case import TestCase as Base


class TestCase(Base):
    def __init__(self, methodName='runTest'):
        super(TestCase, self).__init__(methodName)
        self.fixtures_path = path.realpath(path.join(__file__, "../fixtures"))

    def load_expected(self, name):
        with open(path.join(self.fixtures_path, "asts/{0}.json".format(name)), "r") as f:
            return json.load(f)
