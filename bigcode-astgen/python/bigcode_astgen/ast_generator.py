"""This copied and modified from 150k Python Dataset: http://www.srl.inf.ethz.ch/py150.php
"""

import ast

from bigcode_astgen import normalizer


try:
    _ = bool(type(unicode))
    decode_utf8 = lambda x: x.decode("utf-8")
except NameError:
    unicode = str
    decode_utf8 = lambda x: x


def parse_file(filename, normalize=False):
    """Returns the AST nodes of the given file

    Args:
        filename: path to a file containing a Python program
        normalize: whether the AST should be normalized or not
    """
    with open(filename, "r") as f:
        content = f.read()
    return parse_string(content, normalize=normalize)


def parse_string(content, normalize=False):
    """Returns the AST nodes of the given string

    Args:
        content: string containing a Python program
    """
    return ASTGenerator(content, normalize=normalize).generate_ast()


class ASTGenerator:
    def __init__(self, content, filename="<unknonwn>", normalize=False):
        self.content = content
        self.tree = ast.parse(self.content, filename)
        if normalize:
            self.tree = normalizer.normalize(self.tree)
        self._nodes = []

    def generate_ast(self):
        self._nodes = []
        self.traverse(self.tree)
        return self._nodes

    def gen_identifier(self, identifier, node_type="identifier"):
        pos = len(self._nodes)
        json_node = {"id": pos}
        self._nodes.append(json_node)
        json_node["type"] = node_type
        json_node["value"] = identifier
        return pos

    def traverse_list(self, nodes_list, node_type="list"):
        pos = len(self._nodes)
        json_node = {"id": pos}
        self._nodes.append(json_node)
        json_node["type"] = node_type
        children = []
        for item in nodes_list:
            children.append(self.traverse(item))
        if children:
            json_node["children"] = children
        return pos

    @staticmethod
    def is_try(node):
        return hasattr(ast, "Try") and isinstance(node, ast.Try) or \
               hasattr(ast, "TryExcept") and isinstance(node, ast.TryExcept) or \
               hasattr(ast, "TryFinally") and isinstance(node, ast.TryFinally)

    def traverse(self, node):
        pos = len(self._nodes)
        json_node = {"id": pos}
        self._nodes.append(json_node)
        json_node["type"] = type(node).__name__
        children = []
        if isinstance(node, ast.Name):
            json_node["value"] = node.id
        elif isinstance(node, ast.Num):
            json_node["value"] = unicode(node.n)
        elif hasattr(ast, "arg") and isinstance(node, ast.arg):
            json_node["value"] = unicode(node.arg)
        elif isinstance(node, ast.Str):
            json_node["value"] = decode_utf8(node.s)
        elif isinstance(node, ast.alias):
            json_node["value"] = unicode(node.name)
            if node.asname:
                children.append(self.gen_identifier(node.asname))
        elif isinstance(node, ast.FunctionDef):
            json_node["value"] = unicode(node.name)
        elif isinstance(node, ast.ClassDef):
            json_node["value"] = unicode(node.name)
        elif isinstance(node, ast.ImportFrom):
            if node.module:
                json_node["value"] = unicode(node.module)
        elif isinstance(node, ast.Global):
            for n in node.names:
                children.append(self.gen_identifier(n))
        elif isinstance(node, ast.keyword):
            json_node["value"] = unicode(node.arg)
        # Process children.
        if isinstance(node, ast.For):
            children.append(self.traverse(node.target))
            children.append(self.traverse(node.iter))
            children.append(self.traverse_list(node.body, "body"))
            if node.orelse:
                children.append(self.traverse_list(node.orelse, "orelse"))
        elif isinstance(node, (ast.If, ast.While)):
            children.append(self.traverse(node.test))
            children.append(self.traverse_list(node.body, "body"))
            if node.orelse:
                children.append(self.traverse_list(node.orelse, "orelse"))
        elif isinstance(node, ast.With):
            if hasattr(node, "context_expr"):
                children.append(self.traverse(node.context_expr))
            else:
                children.append(self.traverse_list(node.items))
            if getattr(node, "optional_vars", None):
                children.append(self.traverse(node.optional_vars))
            children.append(self.traverse_list(node.body, "body"))
        elif hasattr(ast, "withitem") and isinstance(node, ast.withitem):
            children.append(self.traverse(node.context_expr))
            if node.optional_vars:
                children.append(self.traverse(node.optional_vars))
        elif self.is_try(node):
            children.append(self.traverse_list(node.body, "body"))
            if hasattr(node, "handlers"):
                children.append(self.traverse_list(node.handlers, "handlers"))
            if node.orelse:
                children.append(self.traverse_list(node.orelse, "orelse"))
            if hasattr(node, "finalbody"):
                children.append(self.traverse_list(node.finalbody, "finalbody"))
        elif isinstance(node, ast.arguments):
            children.append(self.traverse_list(node.args, "args"))
            children.append(self.traverse_list(node.defaults, "defaults"))
            if node.vararg and isinstance(node.vararg, str):
                children.append(self.gen_identifier(node.vararg, "vararg"))
            if node.kwarg and isinstance(node.kwarg, str):
                children.append(self.gen_identifier(node.kwarg, "kwarg"))
        elif isinstance(node, ast.ExceptHandler):
            if node.type:
                children.append(self.traverse_list([node.type], "type"))
            if node.name:
                name = node.name
                if isinstance(node.name, str):
                    name = ast.Name(node.name, None)
                children.append(self.traverse_list([name], "name"))
            children.append(self.traverse_list(node.body, "body"))
        elif isinstance(node, ast.ClassDef):
            children.append(self.traverse_list(node.bases, "bases"))
            children.append(self.traverse_list(node.body, "body"))
            children.append(self.traverse_list(node.decorator_list, "decorator_list"))
        elif isinstance(node, ast.FunctionDef):
            children.append(self.traverse(node.args))
            children.append(self.traverse_list(node.body, "body"))
            children.append(self.traverse_list(node.decorator_list, "decorator_list"))
        else:
            # Default handling: iterate over children.
            for child in ast.iter_child_nodes(node):
                if isinstance(child, (ast.expr_context, ast.operator, ast.boolop, ast.unaryop, ast.cmpop)):
                    # Directly include expr_context, and operators into the type instead of creating a child.
                    json_node["type"] = json_node["type"] + type(child).__name__
                else:
                    children.append(self.traverse(child))

        if isinstance(node, ast.Attribute):
            children.append(self.gen_identifier(node.attr, "attr"))

        if children:
            json_node["children"] = children

        return pos
