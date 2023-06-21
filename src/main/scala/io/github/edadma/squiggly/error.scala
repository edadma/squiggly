package io.github.edadma.squiggly

import scala.util.parsing.input.Positional

def error(elem: Positional, msg: String): String =
  if elem == null || elem.pos == null then msg
  else if elem.pos.line == 1 then s"$msg\n${elem.pos.longString}"
  else s"${elem.pos.line}: $msg\n${elem.pos.longString}"

def problem(elem: Positional, msg: String): Nothing = sys.error(error(elem, msg))

def problem(msg: String): Nothing =
  Console.err.println(msg)
  sys.exit(1)

def warning(elem: Positional, msg: String): Unit = println(error(elem, msg))
