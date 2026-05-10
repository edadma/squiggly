---
title: Regex
summary: matchRE, findRE, replaceRE for pattern-based string operations.
weight: 30
---

Regex builtins use Java's `java.util.regex.Pattern` syntax (the same flavor as Scala's `Regex`).

| Function     | Arity | What |
|--------------|-------|------|
| `matchRE`    | 2     | `matchRE s pattern` — true if the entire string matches |
| `findRE`     | 2     | `findRE s pattern` — first match's groups (a list); empty list when no match |
| `findAllRE`  | 2     | `findAllRE s pattern` — list of group-lists, one per match |
| `replaceRE`  | 3     | `replaceRE s pattern replacement` — every match replaced; `$1`, `$2`, `$0` work in `replacement` |
| `splitRE`    | 2     | `splitRE s pattern` — split on every match, returning the in-between pieces |

## Examples

### Validate

```squiggly
{{ if matchRE .email '^[^@]+@[^@]+\.[^@]+$' }}
  valid
{{ else }}
  invalid
{{ end }}
```

### Extract groups

```squiggly
{{ ymd := findRE .date '^(\d{4})-(\d{2})-(\d{2})$' }}
{{ // ymd is a list: [whole-match, year, month, day] }}
{{ if ymd | nonEmpty }}
  year = {{ ymd[1] }}, month = {{ ymd[2] }}, day = {{ ymd[3] }}
{{ end }}
```

The capture-group behavior was restored in 0.2.3 — earlier 0.2.x lost groups.

### Find every match

```squiggly
{{ for m <- findAllRE .body '\\[\\[([^\\]]+)\\]\\]' }}
  {{ m[1] }}        // each capture group's text
{{ end }}
```

### Replace

```squiggly
{{ replaceRE .body '\\b(\\w+) - (\\w+)\\b' '$1 — $2' }}
```

### Split

```squiggly
{{ for chunk <- splitRE .text '\\s+' }}
  {{ chunk }}
{{ end }}
```
