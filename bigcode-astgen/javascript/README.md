# bigcode-astgen-js

Generate JavaScript files AST in a format compatible with [150k JavaScript Dataset][1].

## Installation

This package can be with npm by running

```
npm i -g bigcode-astgen
```

or by fetching this repository and running

```
cd bigcode-astgen/javascript
npm i -g .
```

## CLI usage

```
bigcode-astgen-js -o <output> <input>
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
* `<output>_failed.txt` - contains a filename per line, with the reason why it could not
 be parsed

Quote your glob pattern so that it is not expanded by your shell.

### Example

#### Normal mode

```
bigcode-astgen-js index.js
```

parse `index.js` and output the result to stdout.

#### Batch mode

```
bigcode-astgen-js --batch -o result/asts "src/**/*.js"
```

parse all `.js` files in `src` directory and output results in the `result` directory
with the prefix `asts` as `asts.json`, `asts.txt` and `asts_failed.txt`.

## NodeJS API

`bigcode-astgen` exports the following functions

### `bigcodeASTGen(options, callback)`

* `options` `{Object}` - should contain the following properties
  * `input` `{String}` - glob expression of the files to process
  * `output` `{String}` - file basename to save the data
* `callback` `{Function}`
  * `err` `{Error | null}`
  * `count` `{Number}` - the number of files processed

### `bigcodeASTGen.processFile(path, output, callback)`

* `path` `{String}` - path of the file to process
* `output` `{String}` - output file to save the AST, outputs to stdout if falsy
* `callback` `{Function}`
  * `err` `{Error | null}`

### `bigcodeASTGen.fromFile`

* `path` `{String}` - path of the file to process
* `callback` `{Function}`
  * `err` `{Error | null}`
  * `ast` `{Array}` - the nodes of the AST in the 150k JavaScript dataset format

### `bigcodeASTGen.fromString`

* `content` `{String}` - a JavaScript program
* return: `{Array}` the nodes of the AST in the 150k JavaScript dataset format

### `bigcodeASTGen.fromNode`

* `root` `{Node}` - the root of the AST parsed by [acorn][2]
* return: `{Array}` the nodes of the AST in the 150k JavaScript dataset format

[1]: http://www.srl.inf.ethz.ch/js150.php
[2]: https://github.com/ternjs/acorn
