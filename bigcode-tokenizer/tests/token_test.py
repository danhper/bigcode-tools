from unittest.case import TestCase

from bigcode_tokenizer.token import Token


class TokenTest(TestCase):
    def test_as_dict(self):
        self.assertEqual(Token("foo").as_dict(), {"type": "foo"})
        self.assertEqual(Token("foo", "bar").as_dict(), {"type": "foo", "value": "bar"})

    def test_eq(self):
        self.assertEqual(Token("foo"), Token("foo"))
        self.assertEqual(Token("foo", "bar"), Token("foo", "bar"))
        self.assertNotEqual(Token("foo", "bar"), Token("foo"))

    def test_hash(self):
        self.assertEqual(hash(Token("foo")), hash(Token("foo")))
        self.assertEqual(hash(Token("foo", "bar")), hash(Token("foo", "bar")))
        self.assertNotEqual(hash(Token("foo", "bar")), hash(Token("foo")))

    def test_repr(self):
        self.assertEqual(str(Token("foo")), "Token(type='foo')")
        self.assertEqual(str(Token("foo", "bar")), "Token(type='foo', value='bar')")
