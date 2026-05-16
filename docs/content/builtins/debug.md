---
title: Debug
summary: context, print, println — what's actually in scope here?
weight: 60
---

When a template isn't rendering what you thought it would, these
builtins help you peek inside without leaving the template.

| Function    | Arity | Signature       | What |
|-------------|-------|-----------------|------|
| `context`   | 0     | `context`       | Print the entire current data context to stdout |
| `print`     | 0+    | `print v ...`   | Print one or more values to stdout, comma-separated, no trailing newline |
| `println`   | 0+    | `println v ...` | Same as `print` but with a trailing newline |

All three are **side-effect-only**: their return value is unit / `null`,
so you generally call them as statements inside a template rather
than interpolating their result. The output goes to the Scala
process's stdout, not to the template's render buffer.

## `context` — dump the whole scope

```squiggly
{{ context }}
```

…dumps everything the template can see at this point — the current
`data`, plus locals introduced by enclosing `with` / `for` / `:=`
blocks. Useful when a field reference unexpectedly renders empty:
drop a `{{ context }}` above it and check whether the field is
actually there.

## `print` / `println` — log a specific value

```squiggly
{{ println 'rendering page:' .page.title }}
{{ print 'tag count =' (length .page.tags) }}
```

## When you need more

For non-trivial debugging it's often easier to instrument from
Scala — write a small `inspect: Any => Any` template function that
`println`s its argument and returns it unchanged, then drop
`{{ inspect .x }}` wherever you want a peek without breaking the
flow. Squiggly's data-context model means everything in scope is just
a Scala value; you have full freedom to instrument it from outside
the template.

## Other useful checks

- **`fileExists path`** — `true` iff a file on disk is readable. Lives
  in [URL & text helpers](../url-and-text/) but often comes up
  during debugging when a template lookup fails.
- **`toString v`** — coerce any value to its `String` form. Handy when
  you want to confirm what a non-string value will look like when
  interpolated.
