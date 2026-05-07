package io.github.edadma.squiggly

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.io.{ByteArrayOutputStream, PrintStream}

/** Coverage for `{{ return }}` and `{{ return expr }}` — both the early-exit
  * effect on output and the value returned by `TemplateRenderer.render(...)`.
  */
class ReturnTests extends AnyFreeSpec with Matchers {

  /** Like `Testing.test`, but also exposes the renderer's return value. */
  private def renderAndReturn(data: Any, template: String): (String, Any) = {
    val ast = TemplateParser.default.parse(template)
    val buf = new ByteArrayOutputStream
    val out = new PrintStream(buf)
    val ret = TemplateRenderer.default.render(data, ast, out)

    (buf.toString, ret)
  }

  "bare return halts further rendering" in {
    val (output, _) = renderAndReturn(
      null,
      "before {{ return }}after",
    )

    output shouldBe "before "
  }

  "return with value yields that value" in {
    val (_, ret) = renderAndReturn(
      null,
      "{{ return 42 }}",
    )

    ret shouldBe BigDecimal(42)
  }

  "return inside a `for` exits all iterations" in {
    val (output, ret) = renderAndReturn(
      null,
      "{{ for n <- [1, 2, 3] }}{{ if n = 2 }}{{ return n }}{{ end }}{{ end }}",
    )

    output shouldBe ""
    ret shouldBe BigDecimal(2)
  }

  "return inside `with` halts and yields" in {
    val (output, ret) = renderAndReturn(
      null,
      "before{{ with 7 }}{{ return . }}{{ end }}after",
    )

    output shouldBe "before"
    ret shouldBe BigDecimal(7)
  }

  "return without a value yields ()" in {
    val (_, ret) = renderAndReturn(null, "{{ return }}")

    ret shouldBe ((): Unit)
  }
}
