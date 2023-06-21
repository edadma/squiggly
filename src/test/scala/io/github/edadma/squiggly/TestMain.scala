package io.github.edadma.squiggly

import pprint.pprintln

def run(): Unit =
  val input = "for 3"
  val ast = TagParser.parse(input, null, 0)

  pprintln(ast)
