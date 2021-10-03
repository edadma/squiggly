package io.github.edadma.squiggly

import java.io.{ByteArrayOutputStream, PrintStream}

trait Testing {

  def test(yaml: String, template: String): String = {
    val data = if (yaml eq null) null else platform.yaml(yaml)
    val ast = Parser.basic.parse(template)
    val buf = new ByteArrayOutputStream
    val out = new PrintStream(buf)

    Console.withOut(out)(Renderer.basic.render(data, ast))
    buf.toString
  }

}
