import io.github.edadma.squiggly.{Parser, Renderer, platform}

object Main extends App {

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
  val data = platform.yaml("{date: 2021-10-04T18:33:25.004Z}")
  //  val template =
  //    """
  //      |{{ define asdfx }}qwer {{ . }} zxcv{{ end }}{{ block asdf . + 2 }}default: {{ . }}{{ end }}
  //      |""".trim.stripMargin
  val template =
    """
    |{{ now.unix }}
    |""".trim.stripMargin
  val ast = Parser.default.parse(template)

  pprint.pprintln(ast)
  Renderer.default.render(data, ast)

}

// todo: tags should be allowed to occupy multiple lines
// todo: implement break/continue https://shopify.github.io/liquid/tags/control-flow/
// todo: implement 'capture' https://shopify.github.io/liquid/tags/variable/
// todo: implement 'unless' https://shopify.github.io/liquid/tags/control-flow/
// todo: implement 'no output' https://shopify.github.io/liquid/tags/template/
// todo: "filters": https://shopify.github.io/liquid/
