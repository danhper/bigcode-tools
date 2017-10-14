# bigcode-tools

[![CircleCI](https://circleci.com/gh/tuvistavie/bigcode-tools.svg?style=svg&circle-token=2508e8ffaf677893dda1ba0bc670bbd06ce137c5)](https://circleci.com/gh/tuvistavie/bigcode-tools)

A set of tools to help working with ["Big Code"][1].

This repository contains multiple tools to fetch source code,
transform source code into AST, visualize generated ASTs or
learn embedding for AST nodes.

The repository is currently composed of the current subprojects

* [bigcode-fetcher](./bigcode-fetcher): Search and fetch source code
* [bigcode-astgen](./bigcode-astgen): Transform source code into JSON ASTs
* [bigcode-ast-tools](./bigcode-ast-tools): Toolset to work with JSON ASTs
* [bigcode-embeddings](./bigcode-embeddings): Generate [token embeddings][2] from ASTs

Take a look at [the tutorial][3] to get started.

[1]: http://learnbigcode.github.io/
[2]: https://en.wikipedia.org/wiki/Word_embedding
[3]: ./doc/tutorial.md
