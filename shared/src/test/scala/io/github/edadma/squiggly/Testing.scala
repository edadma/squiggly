package io.github.edadma.squiggly

import java.io.{ByteArrayOutputStream, PrintStream}

trait Testing {

  def test(yaml: String, template: String): String = {
    val data = if (yaml eq null) null else platform.yaml(yaml)
    val parser = new TemplateParser(template, "{{", "}}", Builtin.functions, Builtin.namespaces)
    val ast = parser.parse
    val buf = new ByteArrayOutputStream
    val out = new PrintStream(buf)

    new Renderer(out, _ => None, Builtin.functions).render(data, ast)
    buf.toString
  }

}
