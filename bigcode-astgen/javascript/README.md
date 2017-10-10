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
bigcode-astgen-js -f <files> -o <output>
```

`<files>` should be a file, or a glob expression to files, and `output`
should be a file basename (without extension) inside existing directory
where the result should be outputted.
Quote your glob pattern so that it is not expanded by your shell.

### Example

```
bigcode-astgen-js -f 'src/**/*.js' -o result/asts
```

parse all JS files in `src` directory and output results in the `result` directory
as `asts.json`, `asts.txt` and `asts_failed.txt`.

## NodeJS API

`bigcode-astgen` exports the following functions

### `bigcodeASTGen(options, callback)`

* `options` `{Object}` - should contain the following properties
  * `files` `{String}` - glob expression of the files to process
  * `output` `{String}` - file basename to save the data
* `callback` `{Function}`
  * `err` `{Error | null}`
  * `count` `{Number}` - the number of files processed

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
