---
title: Builtins
summary: The bundled template functions — strings, numbers, collections, regex, dates, url/text, debug.
weight: 30
---

Squiggly ships with a standard library of ~80 template functions in
`TemplateBuiltin.functions`. Pass it (and any additions of your own)
to the renderer:

```scala
val renderer = new TemplateRenderer(
  functions = TemplateBuiltin.functions ++ myCustomFunctions,
)
```

Every builtin is callable in three equivalent forms:

```squiggly
{{ trim .name }}              // function-call form
{{ .name | trim }}            // pipe form  (data threads in as last arg)
{{ .name.trim }}              // method form (1-arg only)
```

Pages below catalog them by category:

- [Strings](./strings/) — case, whitespace, search, slicing, editing, `printf`
- [Numbers](./numbers/) — math, parse/coerce (`int` / `float` / `bool`), formatted output
- [Collections](./collections/) — lists, maps, slicing, `map` / `filter`, set ops
- [Regex](./regex/) — `findRE` with capture groups
- [Dates](./dates/) — `now` / `time` / `unix` / `format` on `java.time`
- [URL & text helpers](./url-and-text/) — `urlize` / `slugify` / `markdownify` / `emojify` / `jsonStr` / `querify`
- [Debug](./debug/) — `context` / `print` / `println`
