package io.github.edadma.squiggly

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class FunctionTests extends AnyFreeSpec with Matchers with Testing {

  "filter 1" in {
    test(
      "[3, 4, 5, 6]",
      """
        |{{ for e, i <- . | filter `. > 4` -}}
        |  index: {{ i }}, element: {{ e }}
        |{{ end }}
        """.trim.stripMargin
    ) shouldBe
      """
        |index: 0, element: 5
        |index: 1, element: 6
        |""".trim.stripMargin
  }

  "filter 2" in {
    test(
      "[3, 4, 5, 6]",
      """
        |{{ for e, i <- . | filter `2 div .` -}}
        |  index: {{ i }}, element: {{ e }}
        |{{ end }}
        """.trim.stripMargin
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
    test("{a: 'not the default'}", """{{ default 'asdf' .a }}""") shouldBe """not the default"""
  }

  "default 4" in {
    test(null, """{{ .a | default 'asdf' }}""") shouldBe """asdf"""
  }

  "default 5" in {
    test("{a: 'not the default'}", """{{ .a | default 'asdf' }}""") shouldBe """not the default"""
  }

  "default 6" in {
    test(null, """{{ default 'asdf' 'not the default' }}""") shouldBe """not the default"""
  }

  "default 7" in {
    test(null, """{{ 'not the default' | default 'asdf' }}""") shouldBe """not the default"""
  }

}
