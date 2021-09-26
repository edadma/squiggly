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

  "context 1" in {
    test("a: 3",
         """
        |
        |asdf {{ .a }}
        """.trim.stripMargin) shouldBe
      """
        |
        |asdf 3
        """.trim.stripMargin
  }

  "context 2" in {
    test("345",
         """
        |
        |asdf {{ . }}
        """.trim.stripMargin) shouldBe
      """
        |
        |asdf 345
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

}
