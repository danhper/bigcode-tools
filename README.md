# java-transformer

[![CircleCI](https://circleci.com/gh/tuvistavie/java-transformer.svg?style=svg&circle-token=2508e8ffaf677893dda1ba0bc670bbd06ce137c5)](https://circleci.com/gh/tuvistavie/java-transformer)

Parses and transform Java AST.

To create an executable jar, run:

```
sbt assembly
```

## Usage

```
Usage: ast-transformer [options] <project>

  --pretty              pretty format JSON
  -o, --output <value>  file output
  <project>             project to parse
```

## Setup

Add the following to `~/.sbt/0.13/global.sbt`

```scala
resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"
```
