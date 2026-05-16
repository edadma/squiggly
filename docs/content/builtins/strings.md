---
title: Strings
summary: upper, lower, trim, replace, split, join, printf and friends.
weight: 10
---

The string builtins. Most are callable as a function, a pipe target, or
(where the arity allows) a method. The standard squiggly convention is
**data-last**: in pipe form `s | trim` becomes `trim s`, in
`s | replace 'a' 'b'` it becomes `replace 'a' 'b' s`. The tables below
list the **function-call** signature in left-to-right order; pipe form
threads the piped value in as the *last* argument.

## Case & whitespace

| Function       | Arity | Signature                            | What |
|----------------|-------|--------------------------------------|------|
| `upper`        | 1     | `upper s`                            | Uppercase |
| `lower`        | 1     | `lower s`                            | Lowercase |
| `capitalize`   | 1     | `capitalize s`                       | Uppercase the first letter |
| `trim`         | 1     | `trim s`                             | Strip leading + trailing whitespace |
| `ltrim`        | 1     | `ltrim s`                            | Strip leading whitespace only |
| `rtrim`        | 1     | `rtrim s`                            | Strip trailing whitespace only |
| `newline_to_br`| 1     | `newline_to_br s`                    | Replace `\n` with `<br />` |

## Predicates & search

| Function       | Arity | Signature                            | What |
|----------------|-------|--------------------------------------|------|
| `length`       | 1     | `length s`                           | Number of characters (works on lists / maps too) |
| `isEmpty`      | 1     | `isEmpty s`                          | True iff length is zero |
| `nonEmpty`     | 1     | `nonEmpty s`                         | True iff length is non-zero |
| `startsWith`   | 2     | `startsWith prefix s`                | True iff `s` starts with `prefix` |
| `endsWith`     | 2     | `endsWith suffix s`                  | True iff `s` ends with `suffix` |
| `contains`     | 2     | `contains needle s`                  | True iff `s` contains `needle` |

## Slicing & shaping

| Function       | Arity | Signature                            | What |
|----------------|-------|--------------------------------------|------|
| `reverse`      | 1     | `reverse s`                          | Reverse the string |
| `substring`    | 2/3   | `substring from s` / `substring from until s` | Half-open slice; `from`/`until` are character indices |
| `truncate`     | 2/3   | `truncate n s` / `truncate n ellipsis s` | Truncate to `n` words; appends ellipsis (default `…`) when shortened |
| `split`        | 2     | `split sep s`                        | Split on a literal separator; returns a list |

## Editing

| Function       | Arity | Signature                            | What |
|----------------|-------|--------------------------------------|------|
| `replace`      | 3     | `replace old new s`                  | Replace every occurrence of `old` with `new` |
| `remove`       | 2     | `remove substr s`                    | Drop every occurrence of `substr` |
| `removeFirst`  | 2     | `removeFirst substr s`               | Drop only the first occurrence of `substr` |
| `htmlEscape`   | 1     | `htmlEscape s`                       | Escape `&<>'"` for safe HTML embedding |

## Formatted output

| Function       | Arity | Signature                            | What |
|----------------|-------|--------------------------------------|------|
| `printf`       | 1+    | `printf fmt args...`                 | Format-string output (Java `Formatter` rules: `%d` / `%05d` / `%,d` / `%.2f` / `%-10s` / `%x` / `%%`) |

## Examples

```squiggly
{{ .title | upper }}                              CSS HOOK
{{ .author | trim | lower | replace ' ' '-' }}    "ed-maxedon"
{{ .body | split '\n' | length }}                 number of lines

{{ if .filename | endsWith '.md' }}               markdown file
{{ if .body | contains '<!--more-->' }}           has a manual summary cut

{{ split ',' .csv | join ' / ' }}                 reformat

{{ printf '%05d' .count }}                        zero-padded
{{ printf '%,d' .revenue }}                       1,234,567
{{ printf '%-10s|%s' .key .value }}               left-pad first column
```
