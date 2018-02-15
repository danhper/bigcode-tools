# bigcode-tokenizer

Tool to tokenize source code.
This tool uses [Pygments](http://pygments.org/) lexers
to tokenize the code.

## Install

This tool can be installed by running

```
pip install bigcode-tokenizer
```

or by fetching this repository and running

```
cd bigcode-tokenizer
pip install .
```

## CLI usage

```
bigcode-tokenizer -o <output> <input>
```

`<input>` should be a file, or a glob expression to files.
`<output>` is a file where each line is a JSON array of tokens.
Each token is a JSON object containing the `type` and `value` keys.
