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
./bin/java-bigcode-ast -f <files> -o <output-dir>
```

`<files>` should be a file, or a glob expression to files, and `output-dir`
should be an existing directory where the result should be outputted.
The glob expression should be quoted so that it is not expanded by the shell.

### Example

```
java-bigcode-ast -f 'src/**/*.java' -o result
```

parse all `.java` files in `src` directory and output results in the `result` directory

## Java API

Javadoc can be generated to `build/docs` by running

```
./gradlew javadoc
```


[1]: http://www.srl.inf.ethz.ch/py150.php
[2]: http://www.srl.inf.ethz.ch/js150.php
[3]: http://javaparser.org/
