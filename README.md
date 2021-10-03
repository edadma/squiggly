<img align="right" src="logos/squiggly-trans-200.png" alt="squiggly logo">

squiggly
========

![GitHub release (latest by date including pre-releases)](https://img.shields.io/github/v/release/edadma/squiggly?include_prereleases) ![GitHub (Pre-)Release Date](https://img.shields.io/github/release-date-pre/edadma/squiggly) ![GitHub last commit](https://img.shields.io/github/last-commit/edadma/squiggly) ![GitHub](https://img.shields.io/github/license/edadma/squiggly)

*squiggly* is a Scala based string templating engine.

## Overview

*squiggly* is a language, a Scala library, and a Linux command line application for doing string templating.  *squiggly*
can be compared to [Mustache](https://mustache.github.io/) or [Go templates](https://pkg.go.dev/text/template) or
[Liquid](https://shopify.github.io/liquid/), which are all great template languages. Basically, a string template
composed of text and actions (or tags) which are instructions in *squiggly*, is applied to data to produce a textual
output.

Unlike Mustache, *squiggly* is not logicless, but allows basic very logic to be used in templates.

Curly braces or brackets are sometimes referred to as squiggly brackets, which is where the name "squiggly" comes from.

## Installation

### Library

To use the library in your application, include the following in your `project/plugins.sbt`:

```sbt
addSbtPlugin("com.codecommit" % "sbt-github-packages" % "0.5.2")

```

Include the following in your `build.sbt`:

```sbt
resolvers += Resolver.githubPackages("edadma")

libraryDependencies += "io.github.edadma" %%% "squiggly" % "0.1.4"

```

Use the following `import` statement in your code:

```scala
import io.github.edadma.squiggly._

```

### Command line

To use the command line executable, download the file `squiggly` from the root folder of the repository. Make it
executable by typing

```shell
chmod a+x path/to/squiggly
```

then copy it to your `/usr/bin` folder

```shell
sudo cp path/to/scamplate /usr/bin
```

## Basic use

### Library

Here's a simple example of using *squiggly* to render a template in your application.

```scala
import io.github.edadma.squiggly.{Parser, Renderer}

object Main extends App {

  case class Task(task: String, done: Boolean)

  case class User(user: String, tasks: List[Task])

  val data =
    User("ed",
      List(Task("Improve Parser and Renderer API", done = true),
        Task("Code template example", done = false),
        Task("Update README", done = false)))
  val template =
    """
      |<!DOCTYPE html>
      |<html>
      |  <head>
      |    <title>To-Do list</title>
      |  </head>
      |  <body>
      |    <p>
      |      To-Do list for user '{{ .user }}'
      |    </p>
      |    <table>
      |      <tr>
      |        <td>Task</td>
      |        <td>Done</td>
      |      </tr>
      |      {{ for .tasks -}}
      |      <tr>
      |        <td>{{ .task }}</td>
      |        <td>{{ if .done }}Yes{{ else }}No{{ end }}</td>
      |      </tr>
      |      {{- end }}
      |    </table>
      |  </body>
      |</html>
      |""".trim.stripMargin
  val ast = Parser.default.parse(template)

  Renderer.default.render(data, ast)

}

```

output:

```
<!DOCTYPE html>
<html>
  <head>
    <title>To-Do list</title>
  </head>
  <body>
    <p>
      To-Do list for user 'ed'
    </p>
    <table>
      <tr>
        <td>Task</td>
        <td>Done</td>
      </tr>
      <tr>
        <td>Improve Parser and Renderer API</td>
        <td>Yes</td>
      </tr><tr>
        <td>Code template example</td>
        <td>No</td>
      </tr><tr>
        <td>Update README</td>
        <td>No</td>
      </tr>
    </table>
  </body>
</html>
```

### Command line

Type

```
squiggly -h
```

to get the following usage text:

```
Squiggly v0.1.4
Usage: squiggly [options] [[<template>]]

  -a, --ast              pretty print AST
  -d, --data <YAML>      YAML document
  -f, --template <file>  template file
  -h, --help             prints this usage text
  -v, --version          prints the version
  -y, --yaml <file>      YAML data file
  [<template>]           template string
```

Here's a trivial example:

```shell
squiggly -d "{a: 3, b: 4}" "{{ .a }} + {{ .b }} = {{ .a + .b }}"
```

output:

```
3 + 4 = 7
```

## Templates

Squiggly templates are written using a language that is inspired by
both [Hugo](https://gohugo.io/templates/introduction/) and [Liquid](https://shopify.github.io/liquid/). The goal is low
boilerplate and readability. Hugo is compact and boilerplate free, but it is also a *prefix* language where operations
must always precede their operands. Prefix languages tend not to be conducive to readability. Liquid has better infix
syntax but more boilerplate. Both languages have good features that the other is missing.

### Values

*squiggly* values are pieces of data that are either computer in the template or retrieved from the *context* that is
provided when a template is rendered. A value that is written manually inside a template is called a literal value.

#### Literals

- `null`. The null value simply represents no value, and is rendered in a template as an empty string. It corresponds to
  a Scala `null`.
- `true`, `false`. These correspond to the values of the Scala Boolean class.
- numbers. Numbers in *squiggly* are all instances of one type internally: the Scala `BigDecimal` number type. This was
  chosen mainly because it provides exact decimal arithmetic which is desirable when representing currency
  values.  `BigDecimals` can also represent arbitrarily large integers.
- strings. String literals are written between `'` and `'`, or `"` and `"`, whichever is more convenient. Strings may
  contain any of the standard escapes: `\n`, `\uxxxx`, etc.
- lists. List literals are written between `[` and `]` with each item in the list separated by a `,`.
- maps. Map literals are written between `{` and `}` with each property in the map separated by a `,`, and where a
  property is a *key* (property name) and a value separated by a `:`. For example: `{one: 1, two: 2, three: 3}`

The is a special *undefined* value which purposely cannot be expressed literally. It represents a property that is
missing, and is always rendered in a template as an empty string. It is not the same as `null` because a property that
is not missing can contain a `null` value and one may wish to distinguish between them. The undefined value corresponds
to the Scala `()` value, the only instance of the `Unit`
class.

### Tags

Squiggly templates are plain text with the addition of *tags* that are between `{{` and `}}` delimiters. Actually, the
delimiters are configurable. Following are the currently available tags.

#### `{{` value `}}`

where *value* is any valid *squiggly* expression which results in a data value of some kind. The resulting value is
converted to a string of characters if it is not already, and sent to the output stream.

For example `{{ .title }}` will be replaced by the value of a context property called `title`.

#### `{{` `with` value `}}`

The *with* tag simplifies using properties of a data structure.

TO DO

### Functions

TO DO

## Examples

TO DO

## Tests

Unit tests can be run quickly for the JVM platform by typing

```sbt
squigglyJVM / test

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
