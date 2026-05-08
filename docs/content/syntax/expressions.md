---
title: Expressions
summary: Substitution, field access, operators, function calls, pipes.
weight: 10
---

Inside `{{ … }}` you can put any expression. The result is converted to a string and emitted at that position in the output.

## Substitution

The simplest expression is a field access. The `.` prefix references the current data context:

```
{{ .name }}
{{ .page.title }}
{{ .args[0] }}
{{ .map['some key'] }}
```

Bare identifiers (no leading `.`) reference local variables or template
parameters.

## Literals

```
{{ 'a string' }}      single-quoted
{{ "a string" }}      double-quoted (interchangeable)
{{ 42 }}              integer
{{ 3.14 }}            floating point
{{ true }}            boolean
{{ false }}
{{ null }}
```

## Operators

| Class       | Operators |
|-------------|-----------|
| Arithmetic  | `+`, `-`, `*`, `/`, `%` |
| Comparison  | `==`, `!=`, `<`, `<=`, `>`, `>=` |
| Logical     | `and`, `or`, `not` |
| String      | `+` (concatenation) |
| Membership  | `in` (e.g., `'foo' in .tags`) |

```
{{ if .count > 10 and .show }}
  Many.
{{ end }}
```

## Function calls

Any function in the renderer's function map can be called directly. Most builtins are 1-arity and accept a pipe target.

```
{{ upper .name }}
{{ len .pages }}
{{ replace .text 'foo' 'bar' }}
```

## Pipe syntax

The pipe operator `|` threads the previous value as the *first* argument
to the next function. These two are equivalent:

```
{{ .name | upper | trim }}
{{ trim (upper .name) }}
```

Pipes read left-to-right, which is friendlier than nested calls when you have several transforms.

## Method-style calls

Any 1-arity function can be invoked as a method on its receiver:

```
{{ .name.upper.trim }}
{{ .name | upper | trim }}
{{ trim (upper .name) }}
```

All three render the same output.

## Grouping

Parentheses group expressions and force evaluation order:

```
{{ (1 + 2) * 3 }}
{{ if (.count > 0) and (.published) }}…{{ end }}
```

## Conditional expression

The C-style ternary, written with `then` / `else` keywords for readability:

```
{{ if .draft then 'DRAFT' else 'LIVE' }}
```

(Different from the *block-form* `{{ if … }} … {{ end }}` — see [Control flow](/squiggly/syntax/control-flow/).)

## Truthiness

A value is **truthy** unless it's:

- `null`
- `false`
- the empty string `""`
- the empty list / map
- the integer `0` or float `0.0`

`if`, `and`, `or`, and `not` all use this definition.
