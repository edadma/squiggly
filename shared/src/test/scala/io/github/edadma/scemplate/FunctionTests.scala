package io.github.edadma.scemplate

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class FunctionTests extends AnyFreeSpec with Matchers with Testing {

  "filter 1" in {
    test(
      "[3, 4, 5, 6]",
      """
        |{{ for i, e <- . | filter < . > 4 > -}}
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
        |{{ for i, e <- . | filter < 2 div . > -}}
        |  index: {{ i }}, element: {{ e }}
        |{{ end }}
        """.trim.stripMargin
    ) shouldBe
      """
        |index: 0, element: 4
        |index: 1, element: 6
        |""".trim.stripMargin
  }

  "vars 2" in {
    test(null,
         """
        |{{ v := 345 }}{{ v }} {{ v := 678 }}{{ v }}
        """.trim.stripMargin) shouldBe
      """
        |345 678
        """.trim.stripMargin
  }

}
