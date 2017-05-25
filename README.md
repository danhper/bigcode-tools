# java-transformer

[![CircleCI](https://circleci.com/gh/tuvistavie/java-transformer.svg?style=svg&circle-token=2508e8ffaf677893dda1ba0bc670bbd06ce137c5)](https://circleci.com/gh/tuvistavie/java-transformer)

Parses and transform Java AST.

To create an executable jar, run:

```
make jar
```

## Usage

```
ast-transformer 0.1
  Usage: ast-transformer [generate-ast|extract-tokens|generate-dot|generate-vocabulary] <args>...

  Command: generate-ast [options] <project>

    --pretty                 pretty format JSON
    -k, --keep-identifiers   keep program identifiers and values
    -o, --output <value>     output file
    <project>                project to parse
  Command: extract-tokens <project>

    <project>                project to parse
  Command: generate-dot [options] <filepath>

    <filepath>               file to parse
    -o, --output <value>     output file
    -s, --silent             do not output dot to stdout
    --hide-identifiers       do not show tokens
  Command: generate-vocabulary [options] <project>

    <project>                project from which vocabulary should be generated
    -d, --depth <depth1>,<depth2>
                             the depth of the extracted subgraphs
    -o, --output <value>     output file
    -s, --silent             do not output info to stdout

```
