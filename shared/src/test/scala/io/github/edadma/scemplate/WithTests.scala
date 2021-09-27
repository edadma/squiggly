package io.github.edadma.scemplate

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class WithTests extends AnyFreeSpec with Matchers with Testing {

  "with 1" in {
    test(null,
         """
        |{{ with 123 }}{{ . }}{{ end }}
        """.trim.stripMargin) shouldBe
      """
        |123
        """.trim.stripMargin
  }

  "with 2" in {
    test("{a: {b: 3, c: {d: 4}}}",
         """
        |{{ with .a.b }}{{ . }}{{ end }}
        """.trim.stripMargin) shouldBe
      """
        |3
        """.trim.stripMargin
  }

  "with 3" in {
    test(null,
         """
        |{{ with 0 }}{{ . }}{{ else }}else{{ end }}
        """.trim.stripMargin) shouldBe
      """
        |else
        """.trim.stripMargin
  }

  "with 4" in {
    test(null,
         """
        |{{ with false }}{{ . }}{{ else }}else{{ end }}
        """.trim.stripMargin) shouldBe
      """
        |else
        """.trim.stripMargin
  }

  "with 5" in {
    test(null,
         """
        |{{ with '' }}{{ . }}{{ else }}else{{ end }}
        """.trim.stripMargin) shouldBe
      """
        |else
        """.trim.stripMargin
  }

  "with 6" in {
    test("[]",
         """
        |{{ with . }}{{ . }}{{ else }}else{{ end }}
        """.trim.stripMargin) shouldBe
      """
        |else
        """.trim.stripMargin
  }

  "with 7" in {
    test("123",
         """
        |{{ with . }}{{ . }}{{ end }}
        """.trim.stripMargin) shouldBe
      """
        |123
        """.trim.stripMargin
  }

  "with 8" in {
    test("{}",
         """
        |{{ with . }}{{ . }}{{ else }}else{{ end }}
        """.trim.stripMargin) shouldBe
      """
        |else
        """.trim.stripMargin
  }

}
