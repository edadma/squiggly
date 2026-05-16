---
title: Regex
summary: findRE for capturing groups across every match.
weight: 30
---

Squiggly's regex surface is a single, versatile function — `findRE` —
that returns every match (with capture groups) for a Java
`java.util.regex.Pattern` (the same flavour as Scala's `Regex`).
Other regex idioms (full-string match, replace, split-on-pattern)
compose from `findRE` plus the standard string / list builtins.

| Function     | Arity | Signature                              | What |
|--------------|-------|----------------------------------------|------|
| `findRE`     | 2     | `findRE pattern s`                     | All matches — each is a `[whole, group1, group2, …]` list |
| `findRE`     | 3     | `findRE pattern s limit`               | First `limit` matches only |

## What the result looks like

```squiggly
{{ findRE '(\d{4})-(\d{2})-(\d{2})' '2024-03-12 and 2025-01-09' }}
```

…returns `[[2024-03-12, 2024, 03, 12], [2025-01-09, 2025, 01, 09]]` —
one list per match, with the full match first followed by each
capture group's text. Groups that didn't participate render as an
empty string.

## Idioms

### Validate

```squiggly
{{ if findRE '^[^@]+@[^@]+\.[^@]+$' .email | nonEmpty }}
  valid
{{ else }}
  invalid
{{ end }}
```

### Extract groups

```squiggly
{{ ymd := findRE '^(\d{4})-(\d{2})-(\d{2})$' .date }}
{{ if ymd | nonEmpty }}
  year = {{ ymd[0][1] }}, month = {{ ymd[0][2] }}, day = {{ ymd[0][3] }}
{{ end }}
```

`ymd[0]` is the first match's group list; `ymd[0][1]` is its first
capture group.

### Walk every match

```squiggly
{{ for m <- findRE '\[\[([^\]]+)\]\]' .body }}
  {{ m[1] }}        // each match's first capture group
{{ end }}
```

### Cap the work

```squiggly
{{ findRE '\b\w+\b' .body 10 }}    first ten words only
```

## Beyond `findRE`

Squiggly doesn't ship dedicated `matchRE` / `replaceRE` / `splitRE`
builtins yet. Common workarounds:

- **Full-string match:** wrap the pattern in `^…$` and check
  `findRE pattern s | nonEmpty`.
- **Split on pattern:** for literal separators, use `split sep s`.
  Splitting on a regex is on the roadmap.
- **Replace by pattern:** combine `findRE` with `map` and string
  concatenation, or wait for the planned `replaceRE` builtin.
