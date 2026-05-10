---
title: Dates
summary: now, dateFormat, dateParse — built on java.time.
weight: 40
---

Date/time builtins use Java's `java.time` API. Available on JVM and Scala Native (which has a `java.time` shim) — Scala.js works too via `scala-java-time` if you pull it in as a dep.

| Function     | Arity | What |
|--------------|-------|------|
| `now`        | 0     | Current `LocalDateTime` |
| `dateFormat` | 2     | `dateFormat date pattern` — render a date with a `DateTimeFormatter` pattern |
| `dateParse`  | 2     | `dateParse string pattern` — parse a string into a date |

## Examples

```squiggly
{{ now | dateFormat 'yyyy-MM-dd' }}                today
{{ .page.date | dateFormat 'MMMM d, yyyy' }}        December 7, 2025
{{ .page.date | dateFormat 'EEEE' }}                day of week

{{ d := dateParse .feed.pubDate 'EEE, dd MMM yyyy HH:mm:ss Z' }}
{{ d | dateFormat 'yyyy-MM-dd' }}                   re-format
```

## Pattern reference

The patterns are standard `java.time.format.DateTimeFormatter` patterns — see the [JDK reference](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/format/DateTimeFormatter.html#patterns) for the full grammar. Common pieces:

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
