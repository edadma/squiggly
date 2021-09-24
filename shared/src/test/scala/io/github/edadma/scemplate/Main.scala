package io.github.edadma.scemplate

import pprint.pprintln

object Main extends App {

  val input = " asdf "
  val parser = new TemplateParser(input, "{{", "}}")
  val ast = parser.parse

  pprintln(ast)
  println(new Renderer().render(ast))

}
