package io.github.edadma.scemplate

import pprint._

object Main extends App {

  val input = " asdf {{if a < 5}} less than 5 {{else}} greater {{end}} "
  val parser = new TemplateParser(input, "{{", "}}")

  pprintln(parser.tokenize.toList)

}
