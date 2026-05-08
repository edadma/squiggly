---
title: Strings
summary: upper, lower, trim, replace, split, join, and friends.
weight: 10
---

The string builtins. All can be called as a function, a pipe target, or (where the arity allows) a method.

| Function       | Arity | What |
|----------------|-------|------|
| `upper`        | 1     | Uppercase |
| `lower`        | 1     | Lowercase |
| `trim`         | 1     | Strip leading + trailing whitespace |
| `length`       | 1     | Number of characters |
| `len`          | 1     | Alias for `length` (works on lists / maps too) |
| `isEmpty`      | 1     | True if length is zero |
| `nonEmpty`     | 1     | True if length is non-zero |
| `reverse`      | 1     | Reverse the string |
| `replace`      | 3     | `replace s 'old' 'new'` — every occurrence |
| `split`        | 2     | `split s 'sep'` — into a list of strings |
| `join`         | 2     | `join list 'sep'` — concatenate with separator |
| `startsWith`   | 2     | `startsWith s 'prefix'` |
| `endsWith`     | 2     | `endsWith s 'suffix'` |
| `contains`     | 2     | `contains s 'needle'` |
| `indexOf`      | 2     | `indexOf s 'needle'` — `-1` when absent |
| `substring`    | 3     | `substring s start end` — half-open interval |
| `repeat`       | 2     | `repeat s n` — repeat the string n times |

## Examples

```
{{ .title | upper }}                              CSS HOOK
{{ .author | trim | lower | replace ' ' '-' }}    "ed-maxedon"
{{ .body | split '\n' | length }}                 number of lines

{{ if .filename | endsWith '.md' }}               markdown file
{{ if .body | contains '<!--more-->' }}           has a manual summary cut

{{ split .csv ',' | join ' / ' }}                 reformat
```
