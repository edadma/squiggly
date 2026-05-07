<img style="float: right" src="logos/squiggly1-200.png" alt="squiggly logo">

squiggly
========

![Maven Central](https://img.shields.io/maven-central/v/io.github.edadma/squiggly_3)
![GitHub last commit](https://img.shields.io/github/last-commit/edadma/squiggly)
![GitHub](https://img.shields.io/github/license/edadma/squiggly)
![Scala Version](https://img.shields.io/badge/Scala-3.8.3-blue.svg)
![Scala.js Version](https://img.shields.io/badge/Scala.js-1.21.0-blue.svg)
![Scala Native Version](https://img.shields.io/badge/Scala_Native-0.5.11-blue.svg)

*squiggly* is a Scala 3 string templating engine, cross-built for the JVM,
Scala.js, and Scala Native.

## Overview

*squiggly* is a language, a Scala library, and a small command-line application
for doing string templating. It can be compared to
[Mustache](https://mustache.github.io/), [Go templates](https://pkg.go.dev/text/template),
or [Liquid](https://shopify.github.io/liquid/) — a string template composed of
text and *tags* (instructions in *squiggly*) is applied to context data,
producing textual output.

Unlike Mustache, *squiggly* is not logic-less; it allows basic logic in
templates. The expression language is inspired by Hugo and Liquid, with infix
operators, comparison chains, pipes, and method calls.

The name "squiggly" is a colloquialism for curly braces — the tag delimiters.

## Installation

Add the dependency to your `build.sbt`:

```scala
libraryDependencies += "io.github.edadma" %%% "squiggly" % "0.2.2"
```

`%%%` cross-builds against whatever target your project uses (JVM, Scala.js, or
Scala Native).

Use the following `import` in your code:

```scala
import io.github.edadma.squiggly._
```

## Basic use

### Library

```scala
import io.github.edadma.squiggly._

@main def runDemo(): Unit =
  val data =
    Map(
      "user"  -> "ed",
      "tasks" -> List(
        Map("task" -> "Improve Parser and Renderer API", "done" -> true),
        Map("task" -> "Code template example",           "done" -> false),
        Map("task" -> "Update README",                   "done" -> false),
      ),
    )

  val template =
    """<!DOCTYPE html>
      |<html>
      |  <body>
      |    <p>To-do list for user '{{ .user }}'</p>
      |    <ul>
      |      {{ for .tasks -}}
      |      <li>{{ .task }} — {{ if .done }}done{{ else }}pending{{ end }}</li>
      |      {{- end }}
      |    </ul>
      |  </body>
      |</html>
      |""".stripMargin

  val ast = TemplateParser.default.parse(template)

  TemplateRenderer.default.render(data, ast)
```

`TemplateRenderer.render` takes any Scala value (`Map`, `Seq`, `String`, `BigDecimal`,
`Boolean`, `case class`, …) — there is no special data-loading layer.

### Command line

The CLI is invoked through sbt while developing. From the project root:

```shell
sbt 'squigglyJVM/run "Hello, {{ 1 + 2 }}!"'
```

```
Hello, 3!
```

Equivalently on Scala Native:

```shell
sbt 'squigglyNative/run "Hello, {{ 1 + 2 }}!"'
```

For Scala.js, link the program first and run with `node`:

```shell
sbt squigglyJS/fastLinkJS
node js/target/scala-3.8.3/squiggly-fastopt/main.js "Hello, {{ 1 + 2 }}!"
```

#### CLI flags

```
Squiggly Template Engine v0.2.2
Usage: squiggly [options] [[<template>]]

  -a, --ast              pretty print AST
  -d, --data <JSON>      JSON document (string)
  -f, --template <file>  template file
  -h, --help             prints this usage text
  -v, --version          prints the version
  -j, --json <file>      JSON data file
  [<template>]           template string
```

Example with inline JSON data (note shell quoting; for anything more complex
than the simplest case prefer `-j <file>`):

```shell
echo '{"a": 3, "b": 4}' > /tmp/data.json
sbt 'squigglyJVM/run -j /tmp/data.json "{{ .a }} + {{ .b }} = {{ .a + .b }}"'
```

```
3 + 4 = 7
```

## Templates

Squiggly templates are inspired by [Hugo](https://gohugo.io/templates/introduction/)
and [Liquid](https://shopify.github.io/liquid/). The goal is low boilerplate and
readability. Hugo is compact but strictly prefix; Liquid has nicer infix syntax
but more boilerplate. Squiggly takes the readable parts of each.

### Values

#### Literals

- `null` — represents no value; renders as an empty string. Maps to Scala `null`.
- `true`, `false` — booleans.
- _numbers_ — internally `BigDecimal` (exact decimal arithmetic, arbitrary-precision
  integers, no floating-point surprises).
- _strings_ — between `'…'` or `"…"`, whichever is convenient. Standard escapes:
  `\n`, `\t`, `α`, `\\`, etc.
- _lists_ — `[a, b, c]`.
- _maps_ — `{key: value, key: value}`.

There is also an *undefined* value (Scala `()`) that cannot be written literally
— it represents a missing property and renders as an empty string. It is
distinct from `null` because a present property may be explicitly `null`.

#### Expressions

Everything you'd expect: arithmetic (`+ - * / mod ^ \` for integer division,
`++` for string/list concat), comparison chains (`a < b <= c`), boolean
(`and`, `or`, `not`), conditional (`if x then a else b`), index (`.a[0]`),
method (`'hello'.upper`), function application (`upper 'hello'`), and pipes
(`'hello' | upper`).

### Tags

#### `{{ _value_ }}`

Renders a value into the output. `{{ .title }}` substitutes the `title` field
of the current context.

#### `{{ with _value_ }} … {{ else }} … {{ end }}`

Binds the inner context to *value*; runs the falsy branch if the value is
falsy.

#### `{{ for [ _e_ [ , _i_ ] <- ] _value_ }} … [ {{ else }} … ] {{ end }}`

Iterates over a `Seq` or `Map`. Optional element/index bindings.

#### `{{ if _cond_ }} … [ {{ elsif _cond_ }} … ] [ {{ else }} … ] {{ end }}`

Standard branching. `else if` is spelled `elsif`.

#### `{{ match _expr_ }} {{ case _value_ }} … [ {{ else }} … ] {{ end }}`

Switch on a value.

#### `{{ define _name_ }} … {{ end }}` and `{{ block _name_ _expr_ }} … {{ end }}`

Reusable named blocks with override-by-`define`.

#### `{{ _name_ := _expr_ }}`

Bind a variable in the current context.

#### `{{ return [_expr_] }}`

Halt rendering and yield a value back from `TemplateRenderer.render`.

#### `{{ // _comment_ }}` or `{{ /* _comment_ */ }}`

Template comments.

### Built-in functions

~67 builtins: numeric (`abs`, `ceil`, `floor`, `round`, `min`, `max`, `sum`,
`number`); string (`upper`, `lower`, `capitalize`, `length`, `trim`, `ltrim`,
`rtrim`, `htmlEscape`, `newline_to_br`, `urlize`, `urlEncode`, `urlDecode`,
`split`, `startsWith`, `substring`, `truncate`, `remove`, `removeFirst`);
collection (`head`, `last`, `tail`, `append`, `prepend`, `reverse`,
`distinct`, `compact`, `drop`, `dropRight`, `take`, `takeRight`, `slice`,
`join`, `contains`, `length`, `isEmpty`, `nonEmpty`, `toSeq`, `toString`);
set ops (`intersect`, `union`, `symdiff`, `complement`); higher-order
(`filter`, `filterNot`, `map`, `default`); regex (`findRE`); date/time
(`now`, `time`, `unix`, `format` with named formats `:date_full` /
`:date_long` / `:date_medium` / `:date_short` and any `java.time`
`DateTimeFormatter` pattern); misc (`querify`, `partial`, `fileExists`,
`random`, `shuffle`, `print`, `println`, `context`).

## Tests

```shell
sbt squigglyJVM/test
sbt squigglyJS/test
sbt squigglyNative/test
```

Or run the whole cross-build at once:

```shell
sbt test
```

## Contributing

PRs welcome. New features and bug fixes should ship with tests; `scalafmt` is
configured.

## License

[ISC](https://opensource.org/licenses/ISC)
