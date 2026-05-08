---
title: Installation
summary: Add squiggly as an sbt dependency.
weight: 10
---

Squiggly is published on Maven Central as `io.github.edadma:squiggly`. Cross-built for JVM, Scala.js, and Scala Native on Scala 3.

## sbt

```scala
libraryDependencies += "io.github.edadma" %%% "squiggly" % "0.2.3"
```

`%%%` selects the right artifact for your active platform. If you only need the JVM build, `%%` works too.

## scala-cli

```scala
//> using dep "io.github.edadma::squiggly:0.2.3"
```

## Mill

```scala
ivy"io.github.edadma::squiggly:0.2.3"
```

## Requirements

- **Scala 3.8.3** — squiggly is Scala 3 only. There is no 2.13 backport.
- **JVM 17+** for the JVM target.
- **Node 20+** for the Scala.js target.
- **Clang** for the Scala Native target.
