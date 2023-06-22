package io.github.edadma.squiggly

//import io.github.edadma.cross_platform.*
import io.github.edadma.squiggly.{TemplateParser, TemplateRenderer /*, platform*/}

import scala.collection.immutable.VectorMap

@main def run1(): Unit =

  //  case class Task(task: String, done: Boolean)
  //
  //  case class User(user: String, tasks: List[Task])
  //
  //  val data =
  //    User("ed",
  //         List(Task("Improve Parser and Renderer API", done = true),
  //              Task("Code template example", done = false),
  //              Task("Update README", done = false)))
  //  val template =
  //    """
  //      |<!DOCTYPE html>
  //      |<html>
  //      |  <head>
  //      |    <title>To-Do list</title>
  //      |  </head>
  //      |  <body>
  //      |    <p>
  //      |      To-Do list for user '{{ .user }}'
  //      |    </p>
  //      |    <table>
  //      |      <tr>
  //      |        <td>Task</td>
  //      |        <td>Done</td>
  //      |      </tr>
  //      |      {{ for .tasks -}}
  //      |      <tr>
  //      |        <td>{{ .task }}</td>
  //      |        <td>{{ if .done }}Yes{{ else }}No{{ end }}</td>
  //      |      </tr>
  //      |      {{- end }}
  //      |    </table>
  //      |  </body>
  //      |</html>
  //      |""".trim.stripMargin
  //  val data = platform.yaml("{a: 3, b: 4}")
  val data = VectorMap("a" -> 3, "b" -> 4)
  //  val template =
  //    """
  //      |{{ define asdfx }}qwer {{ . }} zxcv{{ end }}{{ block asdf . + 2 }}default: {{ . }}{{ end }}
  //      |""".trim.stripMargin
  //  val template =
  //    """
  //    |{{ 'not the default' | default 'asdf' }}
  //    |""".trim.stripMargin
  val template =
    """
    |asdf {{ unix now }}
    |""".trim.stripMargin
  val ast = TemplateParser.default.parse(template)
  //  val ast = TemplateParser.default.parse(readFile("bulma.min.css"))

  //  pprint.pprintln(ast)
  TemplateRenderer.default.render(data, ast)
