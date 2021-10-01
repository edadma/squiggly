package io.github.edadma.scemplate

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class ComparisonTests extends AnyFreeSpec with Matchers with Testing {

  "comparison 1" in {
    test(null,
         """
        |{{ 3 < 4 }}
        """.trim.stripMargin) shouldBe
      """
        |true
        """.trim.stripMargin
  }

  "comparison 1a" in {
    test(null,
         """
        |{{ 3 < 3 }}
        """.trim.stripMargin) shouldBe
      """
        |false 
        """.trim.stripMargin
  }

  "comparison 1b" in {
    test(null,
         """
        |{{ 3 > 4 }}
        """.trim.stripMargin) shouldBe
      """
        |false
        """.trim.stripMargin
  }

  "comparison 1c" in {
    test(null,
         """
        |{{ 3 > 3 }}
        """.trim.stripMargin) shouldBe
      """
        |false
        """.trim.stripMargin
  }

  "comparison 1d" in {
    test(null,
         """
        |{{ 3 <= 4 }}
        """.trim.stripMargin) shouldBe
      """
        |true
        """.trim.stripMargin
  }

  "comparison 1e" in {
    test(null,
         """
        |{{ 3 <= 3 }}
        """.trim.stripMargin) shouldBe
      """
        |true 
        """.trim.stripMargin
  }

  "comparison 2" in {
    test(null, "{{ 3 >= 4 }}") shouldBe "false"
  }

  "comparison 2a" in {
    test(null, "{{ 3 >= 3 }}") shouldBe "true"
  }

  "comparison 2b" in {
    test(null, "{{ 3 = 4 }}") shouldBe "false"
  }

  "comparison 2c" in {
    test(null, "{{ 3 = 3 }}") shouldBe "true"
  }

  "comparison 2d" in {
    test(null, "{{ 3 != 4 }}") shouldBe "true"
  }

  "comparison 2e" in {
    test(null, "{{ 3 != 3 }}") shouldBe "false"
  }

  "comparison 3" in {
    test("a: 5",
         """
        |{{ 3 < .a < 7 }}
        """.trim.stripMargin) shouldBe
      """
        |true
        """.trim.stripMargin
  }

  "comparison 3a" in {
    test("a: 5",
         """
        |{{ 5 < .a < 7 }}
        """.trim.stripMargin) shouldBe
      """
        |false
        """.trim.stripMargin
  }

  "comparison 3b" in {
    test("a: 5",
         """
        |{{ 3 < .a < 5 }}
        """.trim.stripMargin) shouldBe
      """
        |false
        """.trim.stripMargin
  }

  "comparison 3c" in {
    test("a: 5",
         """
        |{{ 3 < .a < 5 }}
        """.trim.stripMargin) shouldBe
      """
        |false
        """.trim.stripMargin
  }

  "comparison 3e" in {
    test("a: 5",
         """
        |{{ 3 < .a <= 5 }}
        """.trim.stripMargin) shouldBe
      """
        |true
        """.trim.stripMargin
  }

  "comparison 3f" in {
    test("a: 5",
         """
        |{{ 5 <= .a < 7 }}
        """.trim.stripMargin) shouldBe
      """
        |true
        """.trim.stripMargin
  }

  "comparison 4" in {
    test(null,
         """
        |{{ 3 div 12 }}
        """.trim.stripMargin) shouldBe
      """
        |true 
        """.trim.stripMargin
  }

  "comparison 5" in {
    test(null,
         """
        |{{ 3 div 11 }}
        """.trim.stripMargin) shouldBe
      """
        |false 
        """.trim.stripMargin
  }

  "comparison 6" in {
    (the[RuntimeException] thrownBy
      test(null,
           """
           |{{ 'asdf' < 3 }}
        """.trim.stripMargin)).getMessage should startWith("not a number")
  }

  "comparison 7" in {
    (the[RuntimeException] thrownBy
      test(null,
           """
           |{{ 3 < 'asdf' }}
        """.trim.stripMargin)).getMessage should startWith("not a number")
  }

}
