---
title: Dates
summary: now, time, unix, format — built on java.time.
weight: 40
---

Date/time builtins use Java's `java.time` API. Available on JVM, on
Scala Native via the `scala-java-time` shim, and on Scala.js via the
same shim (squiggly's own build pulls them in for those targets).

| Function     | Arity | Signature                              | What |
|--------------|-------|----------------------------------------|------|
| `now`        | 0     | `now`                                  | Current `OffsetDateTime` |
| `time`       | 1     | `time s`                               | Parse an ISO-shaped string — `OffsetDateTime` (`...Z` / `...+HH:MM`), `Instant`, or `LocalDate` |
| `unix`       | 1     | `unix v`                               | Epoch milliseconds for a date / datetime — returns a `BigDecimal` |
| `format`     | 2     | `format pattern v`                     | Render a date with a `DateTimeFormatter` pattern OR a named token (`:date_short` / `:date_long` / `:date_full` / `:date_medium`) |

## Examples

```squiggly
{{ format 'yyyy-MM-dd' now }}                        today

{{ d := time '2024-03-12T00:00:00Z' }}
{{ format 'MMMM d, yyyy' d }}                        March 12, 2024
{{ format ':date_long'  d }}                         March 12, 2024
{{ format ':date_full'  d }}                         Tuesday, March 12, 2024
{{ format ':date_short' d }}                         3/12/24
{{ unix d }}                                         1710201600000

{{ .page.date | format 'EEEE' }}                     day of week
```

## Named tokens

Four shorthand patterns are recognised by `format`. They render
identically across JVM, JS, and Native (locale is pinned to en-US so
the output doesn't drift on a runtime with a different default
locale).

| Token         | Output for `2024-03-12T00:00:00Z`     |
|---------------|---------------------------------------|
| `:date_full`  | `Tuesday, March 12, 2024`             |
| `:date_long`  | `March 12, 2024`                      |
| `:date_medium`| `Mar 12, 2024`                        |
| `:date_short` | `3/12/24`                             |

## Pattern reference

Anything that isn't a `:`-prefixed named token is passed straight to
`java.time.format.DateTimeFormatter.ofPattern`. See the
[JDK reference](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/format/DateTimeFormatter.html#patterns)
for the full grammar. Common pieces:

| Pattern | Meaning           | Example      |
|---------|-------------------|--------------|
| `yyyy`  | Four-digit year   | `2025`       |
| `MM`    | Zero-padded month | `07`         |
| `MMM`   | Short month name  | `Jul`        |
| `MMMM`  | Full month name   | `July`       |
| `dd`    | Zero-padded day   | `02`         |
| `d`     | Day, no padding   | `2`          |
| `EEE`   | Short weekday     | `Mon`        |
| `EEEE`  | Full weekday      | `Monday`     |
| `HH`    | 24-hour hour      | `14`         |
| `mm`    | Minutes           | `05`         |
| `ss`    | Seconds           | `09`         |
| `Z`     | Numeric zone      | `+0500`      |
| `zzz`   | Short zone name   | `EST`        |

A `LocalDate`-typed value (the shape `time '2024-03-12'` produces)
has no time-of-day component — formatting it with `HH:mm` will throw.
Use the full ISO form (`'2024-03-12T00:00:00Z'`) when you need
hour/minute precision.
