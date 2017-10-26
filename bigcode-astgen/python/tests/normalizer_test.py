import ast

from tests import TestCase

from bigcode_astgen import normalizer


class ASTBulkProcessorTest(TestCase):
    def test_normalize_compare(self):
        cases = [
            ("1 < 2 < 3 < 4", "1 < 2 and 2 < 3 and 3 < 4"),
            ("1 < 2 > 3 == 4", "1 < 2 and 2 > 3 and 3 == 4"),
            ("1 < 2 is not None", "1 < 2 and 2 is not None"),
        ]
        for (source, expected) in cases:
            original_ast = ast.parse(source)
            normalized_ast = normalizer.normalize(original_ast)
            expected_ast = ast.parse(expected)
            self.assertAstEqual(normalized_ast, expected_ast)

    def assertAstEqual(self, node1, node2, ignore_keys=None):
        self.assertEqual(type(node1), type(node2))
        if isinstance(node1, list):
            for (ch1, ch2) in zip(node1, node2):
                self.assertAstEqual(ch1, ch2)
        elif isinstance(node1, ast.AST):
            props = self._get_object_keys(node1)
            self.assertEqual(props, self._get_object_keys(node2))
            if ignore_keys is None:
                ignore_keys = ["lineno", "col_offset"]
            for key in props:
                if key not in ignore_keys:
                    self.assertAstEqual(getattr(node1, key), getattr(node2, key))
        else:
            self.assertEqual(node1, node2)


    @staticmethod
    def _get_object_keys(obj):
        return [key for key in dir(obj) if not key.startswith("_")]
