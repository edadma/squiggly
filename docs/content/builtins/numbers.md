---
title: Numbers
summary: abs, ceil, floor, round, max, min, sum, int, float, bool, printf.
weight: 15
---

Squiggly's numeric type is `BigDecimal` — every numeric literal and
every arithmetic operator yields one. The builtins below operate on
that and on values that can be coerced to it.

## Math

| Function | Arity | Signature        | What |
|----------|-------|------------------|------|
| `abs`    | 1     | `abs n`          | Absolute value |
| `ceil`   | 1     | `ceil n`         | Smallest integer ≥ `n` |
| `floor`  | 1     | `floor n`        | Largest integer ≤ `n` |
| `round`  | 1     | `round n`        | Half-away-from-zero rounding to integer |
| `max`    | 2     | `max a b`        | The larger of two |
| `min`    | 2     | `min a b`        | The smaller of two |
| `sum`    | 1     | `sum xs`         | Sum a list of numbers |

## Parse & coerce

| Function | Arity | Signature        | What |
|----------|-------|------------------|------|
| `number` | 1     | `number s`       | Parse a string as `BigDecimal` |
| `int`    | 1     | `int v`          | Truncate toward zero; parses strings |
| `float`  | 1     | `float v`        | Full-precision numeric; parses strings |
| `bool`   | 1     | `bool v`         | Squiggly's truthiness rule — empty string/list/map → false, zero → false, otherwise true |

## Formatted output

| Function | Arity | Signature                  | What |
|----------|-------|----------------------------|------|
| `printf` | 1+    | `printf fmt args...`       | Java `Formatter`-style — `%d` / `%05d` / `%,d` / `%.2f` / `%-10s` / `%x` / `%%` |

## Examples

```squiggly
{{ abs -5 }}                    5
{{ ceil 3.2 }}                  4
{{ round 3.5 }}                 4
{{ max 7 12 }}                  12
{{ sum [1, 2, 3, 4] }}          10

{{ int 3.9 }}                   3        (truncates, doesn't round)
{{ int '42' }}                  42       (parses strings)
{{ float '3.14' }}              3.14
{{ bool '' }}                   false
{{ bool 0 }}                    false
{{ bool 'no' }}                 true     (non-empty string)

{{ printf '%05d'    42 }}             00042
{{ printf '%,d'     1234567 }}        1,234,567
{{ printf '%.2f'    3.14159 }}        3.14
{{ printf '%-10s|'  'hi' }}           hi        |
{{ printf '%s = %d' 'count' 7 }}      count = 7
```

## How `printf` picks Java types

`printf` walks the format string once, matches each `%`-conversion to
its argument in order, and widens `BigDecimal` arguments to either
`Long` (when the conversion is in the `dboxX` family) or `Double`
(otherwise). That means `printf '%d' 7` and `printf '%f' 3.5` both
work without manual `int 7` / `float 3.5` coercions in the template.

Use `%%` for a literal `%`. Unknown conversions surface a Java
`IllegalFormatConversionException` at render time — squiggly doesn't
sanity-check the pattern.
