package io.github.edadma.squiggly

import java.io.{ByteArrayOutputStream, PrintStream}

trait Testing {

  def test(yaml: String, template: String): String = {
    val data = if (yaml eq null) null else platform.yaml(yaml)
    val ast = TemplateParser.default.parse(template)
    val buf = new ByteArrayOutputStream
    val out = new PrintStream(buf)

    TemplateRenderer.default.render(data, ast, out)
    buf.toString
  }

}
