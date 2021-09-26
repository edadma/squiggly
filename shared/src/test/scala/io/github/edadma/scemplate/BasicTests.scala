package io.github.edadma.scemplate

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class BasicTests extends AnyFreeSpec with Matchers with Testing {

  "empty" in {
    test(null, "") shouldBe ""
  }

  "just text 1" in {
    test(null, "asdf") shouldBe "asdf"
  }

  "just text 3" in {
    test(null,
         """
        |asdf
        |
        """.trim.stripMargin) shouldBe "asdf\n"
  }

  "just text 4" in {
    test(null,
         """
        |
        |asdf
        |
        """.trim.stripMargin) shouldBe "\nasdf\n"
  }

  "just text 5" in {
    test(null,
         """
        |
        |asdf
        """.trim.stripMargin) shouldBe
      """
        |
        |asdf
        """.trim.stripMargin
  }

  "ws 1" in {
    test("345",
         """
        |
        |asdf {{ . }} zxcv
        """.trim.stripMargin) shouldBe
      """
        |
        |asdf 345 zxcv
        """.trim.stripMargin
  }

  "ws 2" in {
    test("345",
         """
        |
        |asdf {{- . }} zxcv
        """.trim.stripMargin) shouldBe
      """
        |
        |asdf345 zxcv
        """.trim.stripMargin
  }

  "ws 3" in {
    test("345",
         """
        |
        |asdf {{ . -}} zxcv
        """.trim.stripMargin) shouldBe
      """
        |
        |asdf 345zxcv
        """.trim.stripMargin
  }

  "ws 4" in {
    test("345",
         """
        |
        |asdf {{- . -}} zxcv
        """.trim.stripMargin) shouldBe
      """
        |
        |asdf345zxcv
        """.trim.stripMargin
  }

  "boolean literals" in {
    test("{t: true, f: false}",
         """
        |
        |booleans {{ .t }}, {{ .f }}, {{ true }}, {{ false }}
        """.trim.stripMargin) shouldBe
      """
        |
        |booleans true, false, true, false
        """.trim.stripMargin
  }

  "integer literals" in {
    test("{a: -345, b: 0, c: 345}",
         """
        |
        |ints {{ .a }} {{ .b }} {{ .c }} {{ -345 }} {{ 0 }} {{ 345 }}
        """.trim.stripMargin) shouldBe
      """
        |
        |ints -345 0 345 -345 0 345
        """.trim.stripMargin
  }

  "decimal literals" in {
    test("{N: 6.02214076e23}",
         """
        |
        |Avogadro number {{ .N }} {{ 6.02214076e23 }}
        """.trim.stripMargin) shouldBe
      """
        |
        |Avogadro number 6.02214076E+23 6.02214076E+23
        """.trim.stripMargin
  }

}
