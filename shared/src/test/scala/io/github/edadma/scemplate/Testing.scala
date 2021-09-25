package io.github.edadma.scemplate

trait Testing {

  def test(yaml: String, template: String): String = {
    val data = if (yaml eq null) Map() else platform.yaml(yaml)
    val parser = new TemplateParser(template, "{{", "}}")
    val ast = parser.parse

    new Renderer().render(data, ast)
  }

}
