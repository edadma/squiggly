import io.github.edadma.squiggly.{Parser, Renderer}

object Main extends App {

  case class Task(task: String, done: Boolean)

  case class User(user: String, tasks: List[Task])

  val data =
    User("ed",
         List(Task("Improve Parser and Renderer API", done = true),
              Task("Code template example", done = false),
              Task("Update README", done = false)))
  val template =
    """
      |<!DOCTYPE html>
      |<html>
      |  <head>
      |    <title>To-Do list</title>
      |  </head>
      |  <body>
      |    <p>
      |      To-Do list for user '{{ .user }}'
      |    </p>
      |    <table>
      |      <tr>
      |        <td>Task</td>
      |        <td>Done</td>
      |      </tr>
      |      {{ for .tasks -}}
      |      <tr>
      |        <td>{{ .task }}</td>
      |        <td>{{ if .done }}Yes{{ else }}No{{ end }}</td>
      |      </tr>
      |      {{- end }}
      |    </table>
      |  </body>
      |</html>
      |""".trim.stripMargin
  val ast = Parser.basic.parse(template)

  Renderer.basic.render(data, ast)

}
