---
title: Collections
summary: Operating on lists and maps — head, tail, append, distinct, sort.
weight: 20
---

| Function    | Arity | What |
|-------------|-------|------|
| `len`       | 1     | Length of a list / map / string |
| `isEmpty`   | 1     | True iff the collection has zero items |
| `nonEmpty`  | 1     | True iff the collection has at least one item |
| `head`      | 1     | First element of a list |
| `tail`      | 1     | All but the first |
| `first`     | 1     | Alias for `head` |
| `last`      | 1     | Last element |
| `reverse`   | 1     | Reversed copy |
| `sort`      | 1     | Sorted copy (natural ordering on the elements) |
| `distinct`  | 1     | Drop duplicates, preserving original order |
| `compact`   | 1     | Drop `null` and the empty-tuple `()` from a list |
| `append`    | 2     | `append elem list` — append to the right |
| `prepend`   | 2     | `prepend elem list` — prepend to the left |

## Examples

```
{{ .args | head }}                          first positional arg
{{ .args | tail | join ' ' }}               drop first, rejoin

{{ for n <- .nums | distinct | sort }}      unique then sorted
  {{ n }}
{{ end }}

{{ if .items | nonEmpty }}…{{ end }}        guard against empty list

{{ .tags | append 'featured' | join ' ' }}  add a tag, render
```

## Maps

`for k, v <- m` iterates a map. Many of the list builtins above work on maps too — `len m` returns the entry count, `isEmpty m` checks for an empty map.

To pluck a value by key, use `[…]` indexing in expressions:

```
{{ .config['baseURL'] }}
{{ .frontmatter.tags }}        // dot syntax works for string keys
```
