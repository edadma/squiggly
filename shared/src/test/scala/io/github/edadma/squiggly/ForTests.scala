package io.github.edadma.squiggly

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should._

class ForTests extends AnyFreeSpec with Matchers with Testing {

  "for 1" in {
    test("l: [3, 4]",
         """
        |{{ for .l }}{{ . }}{{ end }}
        """.trim.stripMargin) shouldBe
      """
        |34
        """.trim.stripMargin
  }

  "for 1a" in {
    test("l: [3, 4]",
         """
          |{{ for i <- .l }}[{{ i }}, {{ . }}]{{ end }}
          """.trim.stripMargin) shouldBe
      """
        |[3, 3][4, 4]
        """.trim.stripMargin
  }

  "for 1b" in {
    test("l: [3, 4]",
         """
          |{{ for i, e <- .l }}[{{ i }}, {{ e }}, {{ . }}]{{ end }}
          """.trim.stripMargin) shouldBe
      """
        |[0, 3, 3][1, 4, 4]
        """.trim.stripMargin
  }

  "for 2" in {
    (the[RuntimeException] thrownBy
      test("{a: {b: 3, c: {d: 4}}}",
           """
            |{{ for .a.b }}{{ . }}{{ end }}
            """.trim.stripMargin)).getMessage should startWith("'for' can only be applied to an iterable object")
  }

  "for 3" in {
    test("{a: 3, b: 4, c: 5}",
         """
        |{{ for . }}{{ . }}{{ end }}
        """.trim.stripMargin) shouldBe
      """
        |(a,3)(b,4)(c,5)
        """.trim.stripMargin
  }

  "for 4" in {
    test(null,
         """
        |{{ for 0 }}{{ . }}{{ else }}else{{ end }}
        """.trim.stripMargin) shouldBe
      """
        |else
        """.trim.stripMargin
  }

  "for 5" in {
    test(null,
         """
        |{{ for false }}{{ . }}{{ else }}else{{ end }}
        """.trim.stripMargin) shouldBe
      """
        |else
        """.trim.stripMargin
  }

  "for 6" in {
    test(null,
         """
        |{{ for '' }}{{ . }}{{ else }}else{{ end }}
        """.trim.stripMargin) shouldBe
      """
        |else
        """.trim.stripMargin
  }

  "for 7" in {
    test("[]",
         """
        |{{ for . }}{{ . }}{{ else }}else{{ end }}
        """.trim.stripMargin) shouldBe
      """
        |else
        """.trim.stripMargin
  }

  "for 8" in {
    test("{}",
         """
        |{{ for . }}{{ . }}{{ else }}else{{ end }}
        """.trim.stripMargin) shouldBe
      """
        |else
        """.trim.stripMargin
  }

}
