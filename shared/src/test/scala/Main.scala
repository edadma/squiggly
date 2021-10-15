import io.github.edadma.squiggly.{TemplateParser, TemplateRenderer, platform}

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
  val data = platform.yaml("{a: 3, b: 4}")
  //  val data = null
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
    |{{ querify . }}
    |""".trim.stripMargin
  val ast = TemplateParser.default.parse(template)

  pprint.pprintln(ast)
  TemplateRenderer.default.render(data, ast)

}

// todo: tags should be allowed to occupy multiple lines
// todo: implement break/continue https://shopify.github.io/liquid/tags/control-flow/
// todo: implement 'capture' https://shopify.github.io/liquid/tags/variable/
// todo: implement 'unless' https://shopify.github.io/liquid/tags/control-flow/
// todo: implement 'no output' https://shopify.github.io/liquid/tags/template/
// todo: add boolean test for null and undefined
