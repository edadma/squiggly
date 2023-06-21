package io.github.edadma.squiggly

import java.io.{ByteArrayOutputStream, PrintStream}

trait Testing {

  def test(y: String, template: String): String = {
    val data = if (y eq null) null else yaml(y)
    val ast = TemplateParser.default.parse(template)
    val buf = new ByteArrayOutputStream
    val out = new PrintStream(buf)

    TemplateRenderer.default.render(data, ast, out)
    buf.toString
  }

}
