package io.github.edadma.squiggly

import pprint.pprintln

def run(): Unit =
  val input = "now.unix"
  val ast = TagParser.parse(input, null, 0)

  pprintln(ast)
