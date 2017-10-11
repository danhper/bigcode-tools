import sys
from os import path
from tests import TestCase

from bigcode_astgen import ast_generator


FIXTURES_PATH = path.realpath(path.join(__file__, "../fixtures"))


class ASTGeneratorTest(TestCase):
    def test_parse_empty_string(self):
        result = ast_generator.parse_string("")
        self.assertEqual(result, [{"id": 0, "type": "Module"}])

    def test_parse_string(self):
        with open(path.join(FIXTURES_PATH, "sources/parse_python.py")) as f:
            content = f.read()
        expected = self.load_expected("parse_python")
        result = ast_generator.parse_string(content)
        for i, node in enumerate(result):
            self.assertEqual(i, node["id"])
            del node["id"]
        # only make sure error is not raised for Python 3
        if sys.version_info.major == 2:
            self.assertEqual(result, expected)

    def test_parse_simple_file(self):
        expected = self.load_expected("simple")
        filepath = path.join(FIXTURES_PATH, "sources/simple.py")
        result = ast_generator.parse_file(filepath)
        self.assertEqual(result, expected)

    def test_parse_file(self):
        expected = self.load_expected("parse_python")
        filepath = path.join(FIXTURES_PATH, "sources/parse_python.py")
        result = ast_generator.parse_file(filepath)
        for i, node in enumerate(result):
            self.assertEqual(i, node["id"])
            del node["id"]
        # only make sure error is not raised for Python 3
        if sys.version_info.major == 2:
            self.assertEqual(result, expected)
