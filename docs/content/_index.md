---
title: Squiggly
summary: A small, fast, Scala 3 template engine — Mustache-flavored syntax over an any-data shape.
---

## What it is

Squiggly is a template engine for Scala 3 that takes ordinary HTML / text with `{{ … }}` interpolation and renders it against a `Map[String, Any]` / `List[Any]` / scalar data shape.

```scala
import io.github.edadma.squiggly._

val tmpl = TemplateParser.default.parse(
  "Hello, {{ .name | upper }}!"
)
val out  = new java.io.ByteArrayOutputStream
new TemplateRenderer().render(Map("name" -> "world"), tmpl, new java.io.PrintStream(out))
println(out.toString)
// => Hello, WORLD!
```

The full language has conditionals, loops, partials, named blocks, pattern matching, pipe filters, and a small standard library — see [Syntax](/squiggly/syntax/) and [Builtins](/squiggly/builtins/).

## Why squiggly?

Mostly because the template syntax — `{{ … }}` — is in fact squiggly. The lineage runs through Mustache, Handlebars, Pug, Twig — the whimsy is the genre.

Practically, squiggly exists because there isn't a great minimal Scala 3 template engine that:

- Cross-builds on **JVM, Scala.js, and Scala Native** with no platform-specific deps.
- Operates on the **any-data shape** (`Map[String, Any]` / `List[Any]` / scalars), not a typed binding. Hands-off integration with anything that already produces that shape — TOML / YAML / JSON parsers, frontmatter readers, etc.
- Stays **small and embeddable** — no annotation processors, no codegen, no compile-time DSL.

It's used as the template engine in [juicer](https://juicer.run/), the static site generator powering this site (and juicer's docs).

## Cross-platform

| Target | Status |
|--------|--------|
| JVM (Scala 3.8.3, Java 17+) | ✓ |
| Scala.js (Node 20+) | ✓ |
| Scala Native | ✓ |

316 / 316 tests pass on all three platforms.

## Where to go next
