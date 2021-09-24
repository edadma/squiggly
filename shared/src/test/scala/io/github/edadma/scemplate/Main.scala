package io.github.edadma.scemplate

import pprint._

object Main extends App {

  val input = " asdf {{var}} "
  val parser = new TemplateParser(input, "{{", "}}")

  pprintln(parser.tokenize.toList)

}
