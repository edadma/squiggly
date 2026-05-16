---
title: URL & text helpers
summary: urlize, slugify, urlEncode, querify, jsonStr, markdownify, emojify.
weight: 50
---

Pure value-transform helpers — URL slugging, percent-encoding,
markdown / emoji rendering, JSON-string escape. None of these need a
site config; they are usable from any squiggly host.

`absURL` / `relURL` are explicitly **not** here — those need a
`baseURL` from a renderer-side site config, so they live in the SSG
layer (e.g. juicer's `juicerUrlBuiltins`) rather than in squiggly's
generic standard library.

## Slugs & URLs

| Function    | Arity | Signature           | What |
|-------------|-------|---------------------|------|
| `urlize`    | 1     | `urlize s`          | Hugo-style URL-friendly form — trim, drop non-alnum, hyphenate spaces. Preserves case. |
| `slugify`   | 1     | `slugify s`         | Squiggly's richer slug — lowercase, ASCII-fold Latin diacritics, collapse non-alnum runs to `-`. |
| `urlEncode` | 1     | `urlEncode s`       | Percent-encode for URL paths / query values. `' '` → `'+'`, `'/'` → `'%2F'`. |
| `urlDecode` | 1     | `urlDecode s`       | Reverse of `urlEncode`. |
| `querify`   | 1+    | `querify k v ...`   | Build a `k=v&k=v` query string from alternating key / value args. |

## Rich content

| Function       | Arity | Signature              | What |
|----------------|-------|------------------------|------|
| `markdownify`  | 1     | `markdownify s`        | Render a markdown string to HTML (default `MarkdownConfig`, no syntax highlighter). |
| `emojify`      | 1     | `emojify s`            | Substitute `:shortcode:` tokens with Unicode emoji. |
| `jsonStr`      | 1     | `jsonStr v`            | JSON-string escape — RFC 8259 §7 plus U+2028 / U+2029 (safe inside `<script type="application/ld+json">`). |

## Examples

```squiggly
{{ urlize 'Hello, World!' }}                 Hello-World
{{ slugify 'Café au lait' }}                 cafe-au-lait
{{ slugify 'C++' }}                          c
{{ urlEncode 'a b/c' }}                      a+b%2Fc

{{ 'Hello **world**' | markdownify }}        Hello <strong>world</strong>
{{ ':rocket: shipped!' | emojify }}          🚀 shipped!

{{ jsonStr .page.title }}                    He said \"hi\"

{{ querify 'q' 'cats' 'page' 2 }}            q=cats&page=2
```

## Frameworks that ship their own `markdownify`

`markdownify` here uses the default markdown config — no syntax
highlighter, no per-site extensions. SSG frameworks built on squiggly
(e.g. juicer) typically *shadow* this builtin in their own function
map with a version that uses the site's configured markdown engine.
Both forms coexist: juicer's site-aware version wins on the key
collision; standalone squiggly users get the simple default.
