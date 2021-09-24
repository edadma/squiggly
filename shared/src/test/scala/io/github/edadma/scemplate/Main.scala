package io.github.edadma.scemplate

import pprint._

object Main extends App {

  val tag = " (f $a + 3 b) + 4 | g 'as\\u2100df' y "

  val parser = new TagParser(tag)

  val ast = parser.parseTag

  pprintln(ast)

}
