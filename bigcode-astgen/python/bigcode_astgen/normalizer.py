import ast


def normalize(node):
    if isinstance(node, ast.Compare):
        return normalize_compare(node)
    for key in dir(node):
        if key.startswith("_"):
            continue
        value = getattr(node, key)
        if isinstance(value, ast.AST):
            setattr(node, key, normalize(value))
        elif isinstance(value, list):
            setattr(node, key, [normalize(n) for n in value])
    return node


def normalize_compare(node):
    """Rewrites a compare expression to a `and` expression
    1 < 2 < 3 > 0
    1 < 2 and 2 < 3 and 3 > 0"""
    and_values = []
    left = node.left
    for (op, val) in zip(node.ops, node.comparators):
        comp = ast.Compare(ops=[op],
                           left=left,
                           comparators=[val],
                           lineno=node.lineno,
                           col_offset=node.col_offset)
        and_values.append(comp)
        left = val
    return ast.BoolOp(op=ast.And(),
                      values=and_values,
                      lineno=node.lineno,
                      col_offset=node.col_offset)
