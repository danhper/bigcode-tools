# bigcode-tools

[![CircleCI](https://circleci.com/gh/danhper/bigcode-tools.svg?style=svg&circle-token=2508e8ffaf677893dda1ba0bc670bbd06ce137c5)](https://circleci.com/gh/danhper/bigcode-tools)

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

If you are using this for academic work, we would be thankful if you could cite the following paper.

```
@inproceedings{Perez:2019:CCD:3341883.3341965,
 author = {Perez, Daniel and Chiba, Shigeru},
 title = {Cross-language Clone Detection by Learning over Abstract Syntax Trees},
 booktitle = {Proceedings of the 16th International Conference on Mining Software Repositories},
 series = {MSR '19},
 year = {2019},
 location = {Montreal, Quebec, Canada},
 pages = {518--528},
 numpages = {11},
 url = {https://doi.org/10.1109/MSR.2019.00078},
 doi = {10.1109/MSR.2019.00078},
 acmid = {3341965},
 publisher = {IEEE Press},
 address = {Piscataway, NJ, USA},
 keywords = {clone detection, machine learning, source code representation},
}
```

[1]: http://learnbigcode.github.io/
[2]: https://en.wikipedia.org/wiki/Word_embedding
[3]: ./doc/tutorial.md
