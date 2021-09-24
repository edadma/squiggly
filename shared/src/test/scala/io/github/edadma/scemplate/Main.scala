package io.github.edadma.scemplate

import pprint.pprintln

object Main extends App {

  case class Person(name: String, age: Int)

  val input = "zxcv {{ . }} asdf "
  val data = Person("jonny", 45)
  val parser = new TemplateParser(input, "{{", "}}")
  val ast = parser.parse

  pprintln(ast)
//  println(new Renderer().render(data, ast))

}
