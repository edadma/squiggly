package io.github.edadma.scemplate

trait Testing {

  def test(yaml: String, template: String): String = {
    val data = if (yaml eq null) Map() else platform.yaml(yaml)
    val parser = new TemplateParser(template, "{{", "}}", Builtin.functions, Builtin.namespaces)
    val ast = parser.parse

    TemplateRenderer.defaultRenderer.render(data, ast)
  }

}
