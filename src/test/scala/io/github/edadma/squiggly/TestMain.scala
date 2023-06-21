package io.github.edadma.squiggly

import pprint.pprintln

@main def run(): Unit =
  val input = "3 + 4"
  val ast = TagParser.parse(input, null, 0)

  pprintln(ast)
