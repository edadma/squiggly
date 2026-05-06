package io.github.edadma.squiggly

import java.io.{ByteArrayOutputStream, PrintStream}

trait Testing {

  /** Run a template against caller-supplied data (any Scala value: Map / Seq / Long / etc.). */
  def test(data: Any, template: String): String = {
    val ast = TemplateParser.default.parse(template)
    val buf = new ByteArrayOutputStream
    val out = new PrintStream(buf)

    TemplateRenderer.default.render(data, ast, out)
    buf.toString
  }

  /** Convenience: parse a JSON string into squiggly's data model and run the template. */
  def testJson(json: String, template: String): String =
    test(if (json eq null) null else parseJsonData(json), template)

}
