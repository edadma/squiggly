package io.github.edadma.scemplate

import pprint.pprintln

import scala.language.postfixOps

object Main extends App {

  case class Person(name: String, age: Int)

  //  val data = Map("jonny" -> Person("jonny", 45))
//  val data = List(BigDecimal(3), BigDecimal(4), "asdf")
  val data = BigDecimal(3)
  //  val input = "zxcv {{ with .jonny -}} name: {{ .name }} age: {{ .age }} {{- end }} asdf "
  val input = "[{{ if . = 3 }}three{{ else if . = 4 }}four{{ else }}none{{ end }}]"
  val parser = new TemplateParser(input, "{{", "}}", Builtin.functions)
  val ast = parser.parse

  pprintln(ast)
  println(Renderer.defaultRenderer.render(data, ast))

}
