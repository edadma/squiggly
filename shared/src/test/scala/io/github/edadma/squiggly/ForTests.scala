package io.github.edadma.squiggly

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should._

class ForTests extends AnyFreeSpec with Matchers with Testing {

  "for 1" in {
    testJson("""{"l": [3, 4]}""",
         """
        |{{ for .l }}{{ . }}{{ end }}
        """.trim.stripMargin) shouldBe
      """
        |34
        """.trim.stripMargin
  }

  "for 1a" in {
    testJson("""{"l": [3, 4]}""",
         """
          |{{ for i <- .l }}[{{ i }}, {{ . }}]{{ end }}
          """.trim.stripMargin) shouldBe
      """
        |[3, 3][4, 4]
        """.trim.stripMargin
  }

  "for 1b" in {
    testJson("""{"l": [3, 4]}""",
         """
          |{{ for e, i <- .l }}[{{ i }}, {{ e }}, {{ . }}]{{ end }}
          """.trim.stripMargin) shouldBe
      """
        |[0, 3, 3][1, 4, 4]
        """.trim.stripMargin
  }

  "for 2" in {
    (the[RuntimeException] thrownBy
      testJson("""{"a": {"b": 3, "c": {"d": 4}}}""",
           """
            |{{ for .a.b }}{{ . }}{{ end }}
            """.trim.stripMargin)).getMessage should startWith("'for' can only be applied to an iterable object")
  }

  "for 3" in {
    testJson("""{"a": 3, "b": 4, "c": 5}""",
         """
        |{{ for . }}{{ . }}{{ end }}
        """.trim.stripMargin) shouldBe
      """
        |345
        """.trim.stripMargin
  }

  "for 3a" in {
    testJson("""{"a": 3, "b": 4, "c": 5}""",
         """
        |{{ for v <- . }}{{ . }}{{ end }}
        """.trim.stripMargin) shouldBe
      """
        |345
        """.trim.stripMargin
  }

  "for 3b" in {
    testJson("""{"a": 3, "b": 4, "c": 5}""",
         """
        |{{ for k, v <- . }}<{{ k }}, {{ v }}>{{ end }}
        """.trim.stripMargin) shouldBe
      """
        |<a, 3><b, 4><c, 5>
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
    testJson("[]",
         """
        |{{ for . }}{{ . }}{{ else }}else{{ end }}
        """.trim.stripMargin) shouldBe
      """
        |else
        """.trim.stripMargin
  }

  "for 8" in {
    testJson("{}",
         """
        |{{ for . }}{{ . }}{{ else }}else{{ end }}
        """.trim.stripMargin) shouldBe
      """
        |else
        """.trim.stripMargin
  }

  // Regression: the loop index from `for x, i <- coll` is a Java boxed Int
  // (the runtime stores it via `vars(idx) = i` where i comes from
  // `s.zipWithIndex`). Before the Context.num boxed-Number fix, comparing
  // `i > 0` or `i = $.l.length - 1` threw scala.MatchError because num()
  // only knew how to handle `Num` (BigDecimal alias) and `String`. The
  // `$.` global-root prefix is necessary because the for-loop shifts data
  // to the current element — `.l` inside the body would resolve against
  // the iterated string, not the original list.
  "for-with-index comparison" in {
    testJson("""{"l": ["a", "b", "c"]}""",
         """
          |{{ for x, i <- .l }}{{ if i > 0 }}{{ if i = $.l.length - 1 }} and {{ else }}, {{ end }}{{ end }}{{ x }}{{ end }}
          """.trim.stripMargin) shouldBe
      """
        |a, b and c
        """.trim.stripMargin
  }

}
