from unittest.case import TestCase

from pygments.lexers.jvm import JavaLexer
from pygments import token

from bigcode_tokenizer.tokenizer import JavaTokenizer
from bigcode_tokenizer.token import Token


class JavaTokenizerTest(TestCase):
    @classmethod
    def setUpClass(cls):
        cls.lexer = JavaLexer()

    def setUp(self):
        self.tokenizer = JavaTokenizer()

    def test_skip_tokens_comments(self):
        tokens = [(token.Comment.Multiline, "foo"), (token.Name, "foo")]
        result, skipped = self.tokenizer.skip_tokens(tokens)
        self.assertEqual(result, tokens[1:])
        self.assertEqual(skipped, 1)

    def test_skip_tokens_text(self):
        tokens = [(token.Text, "\n"), (token.Name, "foo")]
        result, skipped = self.tokenizer.skip_tokens(tokens)
        self.assertEqual(result, tokens[1:])
        self.assertEqual(skipped, 1)

    def test_transform_normal_token(self):
        self.assertEqual(self._get_token("this.foo"), Token("Keyword", "this"))

    def test_transform_and_token(self):
        self.assertEqual(self._get_token("&& bar"), Token("Operator", "&&"))

    def test_tokenize(self):
        expected = [Token("Name", "foo"), Token("Operator", "&&"), Token("Name", "bar")]
        self.assertEqual(self.tokenizer.tokenize_string("foo && bar"), expected)

    def _get_token(self, text, start=0):
        return self.tokenizer.get_next_token(self._input(text, start))[0]

    def _input(self, text, start=0):
        result = list(self.lexer.get_tokens(text))
        return result[start:]
