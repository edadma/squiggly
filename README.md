scemplate
=========

![GitHub release (latest by date including pre-releases)](https://img.shields.io/github/v/release/edadma/scemplate?include_prereleases) ![GitHub (Pre-)Release Date](https://img.shields.io/github/release-date-pre/edadma/scemplate) ![GitHub last commit](https://img.shields.io/github/last-commit/edadma/scemplate) ![GitHub](https://img.shields.io/github/license/edadma/scemplate)

*scemplate* is a Scala based string templating engine.  *scemplate* is also a Linux command line application for
executing templates.

## Overview

## Installation

To use the library in you application:

Include the following in your `project/plugins.sbt`:

```sbt
addSbtPlugin("com.codecommit" % "sbt-github-packages" % "0.5.2")

```

Include the following in your `build.sbt`:

```sbt
resolvers += Resolver.githubPackages("edadma")

libraryDependencies += "io.github.edadma" %%% "scemplate" % "0.1.2"

```

Use the following `import` statement in your code:

```scala
import io.github.edadma.scemplate._

```

To use the executable, download the file `scemplate` from the root folder of the repository. Make it executable by
typing

```shell
chmod a+x path/to/scemplate
```

then copy it to your `/usr/bin` folder

```shell
sudo cp path/to/scamplate /usr/bin
```

## Basic use

Type

```
scemplate -h
```

to get the following usage text:

```
Scala Template Engine v0.1.2
Usage: scemplate [options] [[<template>]]

  -a, --ast              pretty print AST
  -d, --data <YAML>      YAML document
  -f, --template <file>  template file
  -h, --help             prints this usage text
  -v, --version          prints the version
  -y, --yaml <file>      YAML data file
  [<template>]           template string
```

For example

```shell
scemplate -d "{a: 3, b: 4}" "{{ .a }} + {{ .b }} = {{ .a + .b }}"
```

output:

```
3 + 4 = 7
```

## API

## Examples

## Tests

Unit tests can be run quickly for the JVM platform by typing

```sbt
scemplateJVM / test

```

Tests for all platforms can be run by typing simply

```sbt
test

```

## Contributing

This project welcomes contributions from the community. Contributions are accepted using GitHub pull requests; for more
information, see
[GitHub documentation - Creating a pull request](https://help.github.com/articles/creating-a-pull-request/).

For a good pull request, we ask you provide the following:

1. Include a clear description of your pull request in the description with the basic "what" and "why"s for the request.
2. The tests should pass as best as you can.
3. The pull request should include tests for the change. A new feature should have tests for the new feature and bug
   fixes should include a test that fails without the corresponding code change and passes after they are applied.
4. If the pull request is a new feature, please include appropriate documentation in the `README.md` file as well.
5. To help ensure that your code is similar in style to the existing code, `scalafmt` should be used.

## License

[ISC]()
