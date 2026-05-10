---
title: URL & text helpers
summary: relURL, absURL, markdownify, emojify — for sites that emit HTML.
weight: 50
---

These helpers cover the common things a static site generator wants — they're the reason juicer ships with a `data` map containing `baseURL` and `link`. If you're using squiggly as a generic template engine outside that context, you may not need them.

| Function       | Arity | What |
|----------------|-------|------|
| `relURL`       | 1     | Site-relative URL (prepends the renderer's `baseURL.path`) |
| `absURL`       | 1     | Absolute URL (prepends `baseURL.base + baseURL.path`) |
| `urlEscape`    | 1     | Percent-encode for use in URL paths |
| `markdownify`  | 1     | Render a markdown string to HTML (uses `io.github.edadma.markdown`) |
| `emojify`      | 1     | Substitute `:smile:` → 😄 (uses `io.github.edadma.emoji`) |

## Examples

```squiggly
{{ '/getting-started/' | relURL }}        /docs/getting-started/
{{ '/getting-started/' | absURL }}        https://example.com/docs/getting-started/

{{ 'Hello **world**' | markdownify }}     Hello <strong>world</strong>

{{ ':rocket: shipped!' | emojify }}       🚀 shipped!
```

## How `relURL` / `absURL` find baseURL

These read `con.renderer.data("baseURL")` — a `BaseURL(base: String, path: String)` value the host application puts in the renderer's `data` map at construction time:

```scala
import io.github.edadma.squiggly.BaseURL

val renderer = new TemplateRenderer(
  data = Map(
    "baseURL" -> BaseURL("https://example.com", "/docs"),
  ),
)
```

If your data doesn't supply this, `relURL` / `absURL` will throw at template evaluation. Skip them and write the URLs out directly if you don't need site-rooting.
