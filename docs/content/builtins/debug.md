---
title: Debug
summary: context, type — what's actually in scope here?
weight: 60
---

Two functions you reach for when a template isn't rendering what you thought it would.

| Function    | Arity | What |
|-------------|-------|------|
| `context`   | 0     | Print the entire current data context to the renderer's output stream |
| `type`      | 1     | Return the runtime type name of an expression's value (e.g. `"java.lang.String"`, `"scala.collection.immutable.Map"`) |

## Examples

```squiggly
{{ context }}
```

…dumps everything the template can see at this point — the current `data`, plus locals introduced by enclosing `with` / `for` / `:=` blocks. Useful when a field reference unexpectedly renders empty: drop a `{{ context }}` above it and check whether the field is actually there.

```squiggly
{{ type .page }}
{{ type .page.tags }}
{{ type .page.weight }}
```

Tells you what concrete class the value is. Helps when an `if` is taking the wrong branch (because, say, your "boolean" is actually a string `"false"` from frontmatter).

## When you need more

For non-trivial debugging, it's often easier to print from Scala — write a small `inspect: Any => Any` template function that `println`s its argument and returns it unchanged, then drop `{{ inspect .x }}` wherever you want a peek. Squiggly's data-context model means everything in scope is just a Scala value; you have full freedom to instrument it from outside the template.
