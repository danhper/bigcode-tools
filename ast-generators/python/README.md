# py-bigcode-ast

Generate Python files AST in a format compatible with [150k Python Dataset][1].

The code is mostly copied from [150k Python Dataset][1] and adapted to work with Python 3.

## CLI usage

```
py-bigcode-ast -f <files> -o <output-dir>
```

`<files>` should be a file, or a glob expression to files, and `output-dir`
should be an existing directory where the result should be outputted.

### Example

```
py-bigcode-ast -f "src/**/*.py" -o result
```

parse all `.py` files in `src` directory and output results in the `result` directory


## Python API

### `bigcode_ast.ast_generator.parse_string`

Returns the AST nodes of the given string

Args:

* `content`: string containing a Python program


### `bigcode_ast.ast_generator.parse_file`

Returns the AST nodes of the given file

Args:

* `filename`: path to a file containing a Python program

### `bigcode_ast.ast_bulk_processor.process_files`

Process all the files matched with the `files_pattern` and output the results in `output_dir`

Args:

* `files_pattern`: a glob pattern containing python files
* `output_dir`: the path to a directory where to output results

## License

I could not find the license of [150k Python Dataset][1] source code so
until further notice, `bigcode_ast/ast_generator.py`, which is mostly copied from there,
does not follow the MIT license as the rest of this project.


[1]: http://www.srl.inf.ethz.ch/py150.php
