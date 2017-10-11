# bigcode-astgen

Tools to convert source code into an AST in JSON format.

## Supported languages

The following languages are currently supported

* [Python 2 and 3](./python)
* [JavaScript](./javascript)
* [Java](./java)

## AST description

We use the format described in [js150][1], without the trailing `0`.

The formatted AST is an array of objects with the following properties:

* (Required) `id`: unique integer identifying current AST node.
* (Required) `type`: string containing type of current AST node.
* (Optional) `value`: string containing value (if any) of the current AST node.
* (Optional) `children`: array of integers denoting children (if any) of the current AST node.

For example, the following program

```javascript
console.log("Hello World!");
```

gives the following AST.

```json
[{"id":0, "type":"Program", "children": [1]},
 {"id":1, "type":"ExpressionStatement", "children": [2]},
 {"id":2, "type":"CallExpression", "children":[3,6]},
 {"id":3, "type":"MemberExpression", "children":[4,5]},
 {"id":4, "type":"Identifier", "value":"console"},
 {"id":5, "type":"Property", "value":"log"},
 {"id":6, "type":"LiteralString", "value":"Hello World!"}]
```

[1]: http://www.srl.inf.ethz.ch/js150.php
