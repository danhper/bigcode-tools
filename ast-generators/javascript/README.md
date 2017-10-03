# bigcode-ast

Generate JavaScript files AST in a format compatible with [150k JavaScript Dataset][1].

## CLI usage

```
bigcode-ast -f <files> -o <output-dir>
```

`<files>` should be a file, or a glob expression to files, and `output-dir`
should be an existing directory where the result should be outputted.

### Example

```
bigcode-ast -f src/**/*.js -o result
```

parse all JS files in `src` directory and output results in the `result` directory

## NodeJS API

`bigcode-ast` exports the following functions

### `bigcodeAST(options, callback)`

* `options` `{Object}` - should contain the following properties
  * `files` `{String}` - glob expression of the files to process
  * `outputDir` `{String}` - path where results should be saved
* `callback` `{Function}`
  * `err` `{Error | null}`
  * `count` `{Number}` - the number of files processed

### `bigcodeAST.fromFile`

* `path` `{String}` - path of the file to process
* `callback` `{Function}`
  * `err` `{Error | null}`
  * `ast` `{Array}` - the nodes of the AST in the 150k JavaScript dataset format

### `bigcodeAST.fromString`

* `content` `{String}` - a JavaScript program
* return: `{Array}` the nodes of the AST in the 150k JavaScript dataset format

### `bigcodeAST.fromNode`

* `root` `{Node}` - the root of the AST parsed by [acorn][2]
* return: `{Array}` the nodes of the AST in the 150k JavaScript dataset format

[1]: http://www.srl.inf.ethz.ch/js150.php
[2]: https://github.com/ternjs/acorn
