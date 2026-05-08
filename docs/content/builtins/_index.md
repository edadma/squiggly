---
title: Builtins
summary: The bundled template functions — strings, lists, regex, dates, debug.
weight: 30
---

Squiggly ships with a standard library of template functions in `TemplateBuiltin.functions`. Pass it (and any additions of your own) to the renderer:

```scala
val renderer = new TemplateRenderer(
  functions = TemplateBuiltin.functions ++ myCustomFunctions,
)
```

Every builtin is callable in two equivalent forms:

```
{{ trim .name }}              // function-call form
{{ .name | trim }}            // pipe form
{{ .name.trim }}              // method form (1-arg only)
```

Pages below catalog them by category.
