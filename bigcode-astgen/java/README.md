# java-bigcode-ast

Generate Java files AST in a format compatible with [150k Python Dataset][1] and
[150k JavaScript Dataset][2].

Files are parsed using [JavaParser][3].

## Building the project

The only dependency is JDK >= 8. The project can be built with the following command.

```
./gradlew build
```

## CLI usage

```
./bin/bigcode-astgen-java [options] <input>
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
* `<output>_failed.txt` - contains a filename per line, with the reason why it co
uld not be parsed

The glob expression should be quoted so that it is not expanded by the shell.

### Example

#### Normal mode

```
bigcode-astgen-java src/main/java/com/tuvistavie/bigcode/astgen/AstGenerator.java
```

parse `src/main/java/com/tuvistavie/bigcode/astgen/AstGenerator.java` and output the result to stdout.

#### Batch mode

```
bigcode-astgen-java --batch -o result/asts "src/**/*.java"
```

parse all `.java` files in `src` directory and output results in the `result` directory
with the prefix `asts`.

## Java API

The project is available on JCenter as `com.tuvistavie.bigcode:astgen:0.1.1`.

Javadoc can be generated to `build/docs` by running

```
./gradlew javadoc
```


[1]: http://www.srl.inf.ethz.ch/py150.php
[2]: http://www.srl.inf.ethz.ch/js150.php
[3]: http://javaparser.org/
