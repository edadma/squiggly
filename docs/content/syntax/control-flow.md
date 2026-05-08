---
title: Control flow
summary: if / for / with / match — the block forms.
weight: 20
---

Squiggly's block-form actions wrap a chunk of template content with conditional, iterative, or scoping logic.

## `if` / `elif` / `else`

```
{{ if .draft }}
  <span class="badge">Draft</span>
{{ elif .scheduled }}
  <span class="badge">Scheduled</span>
{{ else }}
  <span class="badge">Live</span>
{{ end }}
```

`elif` and `else` are optional. Truthiness rules are the same as in expressions — see [Expressions](/squiggly/syntax/expressions/#truthiness).

## `for`

Two forms — value-only and key-value:

```
{{ for p <- .pages }}
  <li>{{ p.title }}</li>
{{ end }}

{{ for k, v <- .frontmatter }}
  <dt>{{ k }}</dt><dd>{{ v }}</dd>
{{ end }}
```

The iteration variable `p` is bound inside the block; it's not visible after `{{ end }}`.

`for` works on lists and maps. Iterating a string treats it as a list of code-point characters.

`else` works on `for`: it runs when the iterable is empty.

```
{{ for p <- .pages }}
  …
{{ else }}
  No pages yet.
{{ end }}
```

## `with`

`with` evaluates an expression once and binds it as the current data context for the block. Inside the block, the leading `.` refers to whatever you passed in.

```
{{ with .author }}
  <a href="{{ .url }}">{{ .name }}</a>
{{ end }}
```

If the bound value is falsy, the block doesn't render at all (and the optional `else` runs):

```
{{ with .editor }}
  Edited by {{ .name }}.
{{ else }}
  No editor.
{{ end }}
```

## `match`

Pattern-match an expression against several literal values:

```
{{ match .status }}
  {{ case 'draft' }}    <span class="warn">Draft</span>
  {{ case 'live' }}     <span class="ok">Live</span>
  {{ case 'archived' }} <span class="muted">Archived</span>
  {{ else }}            <span>Unknown</span>
{{ end }}
```

The `else` branch is optional but recommended — it catches unexpected values.

## Variable assignment

Inside any block, you can introduce a local with `:=`:

```
{{ words := split .body ' ' }}
{{ wordCount := len words }}

This page has {{ wordCount }} words.
```

The variable is scoped to the enclosing block.

## Nesting

All four block forms nest freely:

```
{{ for p <- .pages }}
  {{ if p.draft }}
    {{ /* skip drafts */ }}
  {{ else }}
    <article>{{ p.title }}</article>
  {{ end }}
{{ end }}
```
