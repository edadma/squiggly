---
title: Collections
summary: Operating on lists and maps — head, tail, slice, filter, map, set ops.
weight: 20
---

Squiggly's collection builtins operate on Scala `Seq[?]` values
(lists, vectors) and `Map[?, ?]` values. Anywhere a function takes a
list, an `Iterable` works — strings are *not* iterables here, so
collection-only ops (`head`, `take`, etc.) don't apply to strings.

All builtins follow the data-last convention: in pipe form
`xs | head` becomes `head xs`, `xs | take 3` becomes `take 3 xs`.

## Length & predicates

| Function    | Arity | Signature                | What |
|-------------|-------|--------------------------|------|
| `length`    | 1     | `length xs`              | Element count (also works on strings + maps) |
| `isEmpty`   | 1     | `isEmpty xs`             | True iff the collection has zero elements |
| `nonEmpty`  | 1     | `nonEmpty xs`            | True iff the collection has at least one element |
| `contains`  | 2     | `contains needle xs`     | True iff `xs` contains `needle` (seq) or key (map) |

## Picking elements

| Function     | Arity | Signature                   | What |
|--------------|-------|-----------------------------|------|
| `head`       | 1     | `head xs`                   | First element |
| `last`       | 1     | `last xs`                   | Last element |
| `tail`       | 1     | `tail xs`                   | All but the first element |

## Slicing

| Function     | Arity | Signature                   | What |
|--------------|-------|-----------------------------|------|
| `take`       | 2     | `take n xs`                 | First `n` elements |
| `takeRight`  | 2     | `takeRight n xs`            | Last `n` elements |
| `drop`       | 2     | `drop n xs`                 | All but the first `n` |
| `dropRight`  | 2     | `dropRight n xs`            | All but the last `n` |
| `slice`      | 2/3   | `slice from xs` / `slice from until xs` | Half-open slice by index |

## Reshaping

| Function     | Arity | Signature                   | What |
|--------------|-------|-----------------------------|------|
| `reverse`    | 1     | `reverse xs`                | Reverse the order (also works on strings) |
| `distinct`   | 1     | `distinct xs`               | Drop duplicates, preserving original order |
| `compact`    | 1     | `compact xs`                | Drop `null` and the empty-tuple `()` elements |
| `flatten`    | 1     | `flatten xs`                | Flatten one level of nesting; scalars pass through |
| `append`     | 2     | `append elem xs`            | Append a single element to the right |
| `prepend`    | 2     | `prepend elem xs`           | Prepend a single element to the left |
| `join`       | 2     | `join sep xs`               | Concatenate string elements with `sep` |

## Functional

| Function     | Arity | Signature                   | What |
|--------------|-------|-----------------------------|------|
| `map`        | 2     | `` map `expr` xs ``         | Transform each element; `expr` is a non-strict expression with `.` bound to the element |
| `filter`     | 2     | `` filter `pred` xs ``      | Keep elements where `pred` is truthy |
| `filterNot`  | 2     | `` filterNot `pred` xs ``   | Keep elements where `pred` is falsy |

The `expr` / `pred` arguments are wrapped in **backticks** so they
evaluate lazily, per element. Inside the expression `.` refers to the
current element.

## Set operations

| Function     | Arity | Signature                   | What |
|--------------|-------|-----------------------------|------|
| `intersect`  | 2     | `intersect a b`             | Elements in both `a` and `b` |
| `union`      | 2     | `union a b`                 | Elements in either `a` or `b` (duplicates collapsed) |
| `symdiff`    | 2     | `symdiff a b`               | Symmetric difference — in one but not both |
| `complement` | 2     | `complement a b`            | Elements in `a` but not in `b` |

## Conversion

| Function     | Arity | Signature                   | What |
|--------------|-------|-----------------------------|------|
| `toSeq`      | 1     | `toSeq it`                  | Materialise any `Iterable` into a `Seq` |
| `sum`        | 1     | `sum xs`                    | Sum the numeric elements |

## Examples

```squiggly
{{ .args | head }}                            first positional arg
{{ .args | tail | join ' ' }}                 drop first, rejoin

{{ for n <- .nums | distinct }}               unique elements
  {{ n }}
{{ end }}

{{ if .items | nonEmpty }}…{{ end }}          guard against empty list

{{ .tags | append 'featured' | join ' ' }}    add a tag, render

{{ filter `.published` .posts | length }}     count published posts
{{ map `.title | upper` .pages }}             every title in caps

{{ slice 0 3 .posts }}                        first three
```

## Maps

`for k, v <- m` iterates a map. Many of the list builtins above also
work on maps — `length m` returns the entry count, `isEmpty m` checks
for an empty map, `contains 'key' m` checks for a key.

To pluck a value by key, use `[…]` indexing in expressions:

```squiggly
{{ .config['baseURL'] }}
{{ .frontmatter.tags }}        // dot syntax works for string keys
```
