package io.github.edadma.squiggly

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class FunctionTests extends AnyFreeSpec with Matchers with Testing {

  "filter 1" in {
    testJson(
      "[3, 4, 5, 6]",
      """
        |{{ for e, i <- . | filter `. > 4` -}}
        |  index: {{ i }}, element: {{ e }}
        |{{ end }}
        """.trim.stripMargin,
    ) shouldBe
      """
        |index: 0, element: 5
        |index: 1, element: 6
        |""".trim.stripMargin
  }

  "filter 2" in {
    testJson(
      "[3, 4, 5, 6]",
      """
        |{{ for e, i <- . | filter `2 div .` -}}
        |  index: {{ i }}, element: {{ e }}
        |{{ end }}
        """.trim.stripMargin,
    ) shouldBe
      """
        |index: 0, element: 4
        |index: 1, element: 6
        |""".trim.stripMargin
  }

  "default 1" in {
    test(null, """{{ default 'asdf' '' }}""") shouldBe """asdf"""
  }

  "default 2" in {
    test(null, """{{ default 'asdf' .a }}""") shouldBe """asdf"""
  }

  "default 3" in {
    testJson("""{"a": "not the default"}""", """{{ default 'asdf' .a }}""") shouldBe """not the default"""
  }

  "default 4" in {
    test(null, """{{ .a | default 'asdf' }}""") shouldBe """asdf"""
  }

  "default 5" in {
    testJson("""{"a": "not the default"}""", """{{ .a | default 'asdf' }}""") shouldBe """not the default"""
  }

  "default 6" in {
    test(null, """{{ default 'asdf' 'not the default' }}""") shouldBe """not the default"""
  }

  "default 7" in {
    test(null, """{{ 'not the default' | default 'asdf' }}""") shouldBe """not the default"""
  }

  // The `format` builtin auto-parses ISO date strings; the named formats
  // (`:date_full` / `:date_long` / `:date_medium` / `:date_short`) and
  // arbitrary java.time DateTimeFormatter patterns are both supported.
  "format 1" in {
    testJson("\"2021-03-04\"", """{{ format ':date_full' . }}""") shouldBe "Thursday, March 4, 2021"
  }

  "format 2" in {
    testJson("\"2021-03-04\"", """{{ . | format ':date_long' }}""") shouldBe "March 4, 2021"
  }

  "format 3" in {
    testJson("\"2021-03-04\"", """{{ format ':date_medium' . }}""") shouldBe "Mar 4, 2021"
  }

  "format 4" in {
    testJson("\"2021-03-04\"", """{{ format ':date_short' . }}""") shouldBe "3/4/21"
  }

  // Format string is a java.time `DateTimeFormatter` pattern. The original
  // YAML-era test used "D MMM YY" (Hugo-style codes); the equivalent
  // java.time pattern is "d MMM uu".
  "format 5" in {
    testJson("\"2021-03-04\"", """{{ format 'd MMM uu' . }}""") shouldBe "4 Mar 21"
  }

}
