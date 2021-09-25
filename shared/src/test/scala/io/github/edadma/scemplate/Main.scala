package io.github.edadma.scemplate

import pprint.pprintln

object Main extends App {

  case class Person(name: String, age: Int)

//  val input = "zxcv {{ with .jonny -}} name: {{ .name }} age: {{ .age }} {{- end }} asdf "
  val input = "{{ now .Unix.asdf (a .b).c }}"
  val data = Map("jonny" -> Person("jonny", 45))
  val parser = new TemplateParser(input, "{{", "}}")
  val ast = parser.parse

  pprintln(ast)
//  println(new Renderer().render(data, ast))

}
