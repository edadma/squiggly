package io.github.edadma.squiggly

import pprint.pprintln

def run(): Unit =
  val input = "now.unix"
  val ast = TagParser.parse(input)

  pprintln(ast)
