package io.github.edadma.scemplate

import pprint.pprintln

import scala.language.postfixOps

object Main extends App {

  case class Person(name: String, age: Int)

  //  val data = Map("jonny" -> Person("jonny", 45))
  val data = List(BigDecimal(3), BigDecimal(4), BigDecimal(5), BigDecimal(6))
  //  val data = BigDecimal(5)
  //  val input = "zxcv {{ with .jonny -}} name: {{ .name }} age: {{ .age }} {{- end }} asdf "
  val input = "{{ for i, e <- . | filter < . > 3 > | take 2 }}index: {{ i }}, element: {{ e }}{{ '\\n' }}{{ end }}"
//  val input = "{{ m := {a: 3, b: 4} }}{{ m['a'] }}"
  val parser = new TemplateParser(input, "{{", "}}", Builtin.functions, Builtin.namespaces)
  val ast = parser.parse

  pprintln(ast)
  println(TemplateRenderer.defaultRenderer.render(data, ast))

}

// todo: https://pkg.go.dev/text/template#hdr-Arguments

// todo: https://gohugo.io/functions/index-function/; do indexing the way liquid does [...]
