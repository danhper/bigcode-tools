# bigcode-astgen-py

Generate Python files AST in a format compatible with [150k Python Dataset][1].

The code is mostly copied from [150k Python Dataset][1] and adapted to work with Python 3.

Note that this tool will only be able to parse the version of Python it is running,
as it is internally using Python `ast` module, which uses the current Python parser.

## Install

This tool can be installed by running

```
pip install bigcode-astgen
```

or by fetching this repository and running

```
cd ast-generators/python
pip install .
```

## CLI usage

```
bigcode-astgen-py -o <output> <input>
```

`<input>` should be a file, or a glob expression to files.

### Normal mode

In normal mode, `<input>` is interpreted as a filename and the resulting AST
is outputed in `<output>` if provided, else printed to `stdout`.

### Batch mode

In batch mode, `<input>` is interpreted as a glob, and all matching files
are parsed. `<output>` is a prefix and `<output>.json`, `<output>.txt` and
`<output>_failed.txt` files will be created.

* `<output>.json` - contains a JSON formatted AST per line
* `<output>.txt` - contains a filename per line, in the same order as `<output>.json`
* `<output>_failed.txt` - contains a filename per line, with the reason why it could not be parsed

### Example

#### Normal mode

```
bigcode-astgen-py bigcode_astgen/normalizer.py
```

parse `bigcode_astgen/normalizer.py` and output the result to stdout.

#### Batch mode

```
bigcode-astgen-py --batch -o result/asts "src/**/*.py"
```

parse all `.py` files in `src` directory and output results in the `result` directory
with the prefix `asts`.


## Python API

### `bigcode_astgen.ast_generator.parse_string`

Returns the AST nodes of the given string

Args:

* `content`: string containing a Python program


### `bigcode_astgen.ast_generator.parse_file`

Returns the AST nodes of the given file

Args:

* `filename`: path to a file containing a Python program

### `bigcode_astgen.ast_bulk_processor.process_files`

Process all the files matched with the `files_pattern` and output the results in `output_dir`

Args:

* `files_pattern`: a glob pattern containing python files
* `output`: the filename (without extension) where to output results

## License

I could not find the license of [150k Python Dataset][1] source code from which
`bigcode_astgen/ast_generator.py` is copied.
Therefore, until further notice, this project does not follow the MIT license as the rest of the repository.


[1]: http://www.srl.inf.ethz.ch/py150.php
