package io.github.edadma.scemplate

import pprint._

object Main extends App {

  val input = " asdf "
  val parser = new TemplateParser(input, "{{", "}}")

  pprintln(parser.parse)

}
