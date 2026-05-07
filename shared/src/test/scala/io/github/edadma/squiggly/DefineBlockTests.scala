package io.github.edadma.squiggly

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.io.{ByteArrayOutputStream, PrintStream}
import scala.collection.mutable

/** End-to-end coverage of `{{ define name }}body{{ end }}`,
  * `{{ block name expr }}body{{ end }}`, the partials hook on
  * `TemplateRenderer`, and the cross-cutting interactions between them.
  */
class DefineBlockTests extends AnyFreeSpec with Matchers {

  /** Render with a caller-supplied `TemplateRenderer` so we can wire up
    * partials. The default `Testing.test` always uses the singleton
    * `TemplateRenderer.default`, which has an empty partials loader.
    */
  private def runWith(renderer: TemplateRenderer, data: Any, template: String): String = {
    val ast = TemplateParser.default.parse(template)
    val buf = new ByteArrayOutputStream
    val out = new PrintStream(buf)

    renderer.render(data, ast, out)
    buf.toString
  }

  "block (no override)" in {
    val template =
      """
        |{{ block greeting 'Ed' }}Hello, {{ . }}!{{ end }}
        """.trim.stripMargin

    val renderer = new TemplateRenderer(blocks = new mutable.HashMap[String, TemplateAST])

    runWith(renderer, null, template) shouldBe "Hello, Ed!"
  }

  "block + define override" in {
    val template =
      """
        |{{ define greeting }}Hi there, {{ . }}.{{ end }}{{ block greeting 'Ed' }}Hello, {{ . }}!{{ end }}
        """.trim.stripMargin

    val renderer = new TemplateRenderer(blocks = new mutable.HashMap[String, TemplateAST])

    runWith(renderer, null, template) shouldBe "Hi there, Ed."
  }

  "define-only emits nothing at definition site" in {
    val template =
      """
        |{{ define unused }}should not appear{{ end }}before-after
        """.trim.stripMargin

    val renderer = new TemplateRenderer(blocks = new mutable.HashMap[String, TemplateAST])

    runWith(renderer, null, template) shouldBe "before-after"
  }

  "partial loaded from a TemplateLoader" in {
    val partials: TemplateLoader = {
      case "greet" => Some(TemplateParser.default.parse("Hello, {{ . }}!"))
      case _       => None
    }
    val renderer = new TemplateRenderer(partials = partials)

    runWith(renderer, "Ed", """{{ partial 'greet' . }}""") shouldBe "Hello, Ed!"
  }

  "partial with no data argument inherits null context" in {
    val partials: TemplateLoader = {
      case "static" => Some(TemplateParser.default.parse("static body"))
      case _        => None
    }
    val renderer = new TemplateRenderer(partials = partials)

    runWith(renderer, null, """{{ partial 'static' }}""") shouldBe "static body"
  }

  "missing partial errors" in {
    val renderer = new TemplateRenderer(partials = _ => None)

    (the[RuntimeException] thrownBy runWith(
      renderer,
      null,
      """{{ partial 'absent' . }}""",
    )).getMessage should include("absent")
  }
}
