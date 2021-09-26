package io.github.edadma.scemplate

import pprint.pprintln

import scala.language.postfixOps

object Main extends App {

  case class Person(name: String, age: Int)

  //  val input = "zxcv {{ with .jonny -}} name: {{ .name }} age: {{ .age }} {{- end }} asdf "
  val input = "items: {{range .}}{{ . }} {{end}}"
//  val data = Map("jonny" -> Person("jonny", 45))
  val data = List(3, 4, "asdf")
  val parser = new TemplateParser(input, "{{", "}}")
  val ast = parser.parse

  pprintln(ast)
  println(Renderer.defaultRenderer.render(data, ast))

}
