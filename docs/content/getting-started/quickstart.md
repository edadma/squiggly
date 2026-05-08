---
title: Quickstart
summary: Parse a template, render it against some data, in three lines.
weight: 20
---

A template is a string of HTML or text with `{{ … }}` interpolation. Squiggly's API has two halves:

- **`TemplateParser`** — parses a template string into an AST.
- **`TemplateRenderer`** — renders an AST against a data shape.

```scala
import io.github.edadma.squiggly._

val parser   = TemplateParser.default
val renderer = new TemplateRenderer()

val ast = parser.parse(
  """{{ for p <- .pages }}
    |  - {{ p.title | upper }}
    |{{ end }}""".stripMargin,
)

val data = Map(
  "pages" -> List(
    Map("title" -> "Hello"),
    Map("title" -> "Goodbye"),
  ),
)

renderer.render(data, ast)
// =>
//   - HELLO
//   - GOODBYE
```

That's it. The any-data shape (`Map[String, Any]` / `List[Any]` / scalars) is what `data` is — anything that can be a YAML / TOML / JSON document fits without translation.

## Customizing the renderer

`TemplateRenderer` takes four optional parameters:

| Parameter   | What |
|-------------|------|
| `partials`  | A `TemplateLoader` (`String => Option[TemplateAST]`) used by `{{ partial 'name' . }}` |
| `blocks`    | Mutable `HashMap[String, TemplateAST]` used by `{{ define name }}` / `{{ block name . }}` — pass an existing one if you want to share named blocks across multiple renders |
| `functions` | Map of custom template functions to merge with the builtin set |
| `data`      | Renderer-wide data — read inside template functions via `con.renderer.data` |

```scala
val renderer = new TemplateRenderer(
  partials  = name => myPartialMap.get(name),
  functions = TemplateBuiltin.functions ++ myFunctions,
  data      = Map("siteUrl" -> "https://example.com"),
)
```

## Going deeper

- [Syntax](/squiggly/syntax/) — every form the parser accepts (substitution, control flow, partials, blocks, pipe filters, expressions).
- [Builtins](/squiggly/builtins/) — the bundled string / list / regex / date functions.
