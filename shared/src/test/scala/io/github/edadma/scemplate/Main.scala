package io.github.edadma.scemplate

import pprint.pprintln

import scala.language.postfixOps

object Main extends App {

  case class Person(name: String, age: Int)

  //  val data = Map("jonny" -> Person("jonny", 45))
  val data = List(BigDecimal(3), BigDecimal(4))
//  val data = BigDecimal(5)
  //  val input = "zxcv {{ with .jonny -}} name: {{ .name }} age: {{ .age }} {{- end }} asdf "
  val input = "{{ . | map <. + 2> }}"
  val parser = new TemplateParser(input, "{{", "}}", Builtin.functions)
  val ast = parser.parse

  pprintln(ast)
  println(Renderer.defaultRenderer.render(data, ast))

}
