package io.github.edadma.squiggly

import pprint.pprintln

@main def run(): Unit =
  val input = "default 'asdf' .a"
  val ast = TagParser.parse(input, null, 0)

  pprintln(ast)
