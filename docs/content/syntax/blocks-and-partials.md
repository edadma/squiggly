---
title: Blocks and partials
summary: define / block / partial — composing templates from pieces.
weight: 30
---

Two ways to share template content across multiple files, with quite different shapes.

## Partials

A **partial** is a template loaded by name from a `TemplateLoader`. The renderer looks up the name through the loader's `String => Option[TemplateAST]` callback, then renders the result with whatever data context you pass in.

```squiggly
{{ partial 'topbar' . }}
{{ partial 'page-toc' .page }}
{{ partial 'footer' (with title='Hello') }}
```

The first argument is a string literal — the name. The second is the data context to render with. Use `.` to pass the current context unchanged.

Partials are **dynamic** — looked up at render time, useful when the partial isn't known until you've parsed the templates.

Wire up partials by passing a loader to the renderer:

```scala
val partials: TemplateLoader = name => myPartialMap.get(name)
val renderer = new TemplateRenderer(partials = partials)
```

## Define / block

`define` and `block` work together to support a "fill-in-the-blanks" layout pattern. A *parent* template declares slots with `{{ block name . }} … {{ end }}` (with optional default content). A *child* template populates those slots with `{{ define name }} … {{ end }}`.

The renderer's `blocks` field is a `mutable.HashMap[String, TemplateAST]` carrying the bindings; render the child first to populate it, then render the parent.

### A two-pass example

`baseof.html` (the parent layout):

```squiggly
<!DOCTYPE html>
<html>
<head><title>{{ .page.title }}</title></head>
<body>
  <main>
    {{ block content . }}
      <p>Default content</p>
    {{ end }}
  </main>
</body>
</html>
```

`page.html` (the child):

```squiggly
{{ define content }}
  <article>
    <h1>{{ .page.title }}</h1>
    {{ .body }}
  </article>
{{ end }}
```

To render the page:

```scala
val parent = parser.parse(baseof)
val child  = parser.parse(page)

val renderer = new TemplateRenderer()

renderer.render(data, child)   // first pass: populates `define` blocks
renderer.render(data, parent)  // second pass: `block content` finds the
                               // populated definition and substitutes
```

The first render writes nothing visible — its purpose is the `define` side effect.

This is the pattern juicer uses for its `_default/baseof.html` + `_default/file.html` / `_default/folder.html` layout combinations.

### Default content

`{{ block name . }} … {{ end }}` renders its inner content if no `define` populated the block.

```squiggly
{{ block sidebar . }}
  <p>No sidebar configured.</p>
{{ end }}
```

If `sidebar` was never defined, the default is what gets rendered.

## When to pick which

| Situation | Use |
|-----------|-----|
| Reusable fragment with its own data context (header, card, badge) | **Partial** |
| "Outer shell" + variable inner content per page kind | **define / block** |
| Many similar pages with different bodies | **define / block** |
| Optional decorations chosen at render time | **Partial** |

Both compose well — your `baseof` block can `{{ partial 'topbar' . }}` inside it.
