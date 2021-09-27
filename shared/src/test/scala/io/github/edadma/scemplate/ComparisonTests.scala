package io.github.edadma.scemplate

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class ComparisonTests extends AnyFreeSpec with Matchers with Testing {

  "comparison 1" in {
    test(null,
         """
        |{{ 3 < 4 }} {{ 3 < 3 }} {{ 3 > 4 }} {{ 3 > 3 }} {{ 3 <= 4 }} {{ 3 <= 3 }}
        """.trim.stripMargin) shouldBe
      """
        |true false false false true true 
        """.trim.stripMargin
  }

  "comparison 2" in {
    test(null,
         """
        |{{ 3 >= 4 }} {{ 3 >= 3 }} {{ 3 = 4 }} {{ 3 = 3 }} {{ 3 != 4 }} {{ 3 != 3 }}
        """.trim.stripMargin) shouldBe
      """
        |false true false true true false
        """.trim.stripMargin
  }

  "comparison 3" in {
    test("a: 5",
         """
        |{{ 3 < .a < 7 }} {{ 5 < .a < 7 }} {{ 3 < .a < 5 }} {{ 3 < .a <= 5 }} {{ 5 <= .a < 7 }}
        """.trim.stripMargin) shouldBe
      """
        |true false false true true
        """.trim.stripMargin
  }

}
