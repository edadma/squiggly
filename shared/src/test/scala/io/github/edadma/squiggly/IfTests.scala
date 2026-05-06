package io.github.edadma.squiggly

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class IfTests extends AnyFreeSpec with Matchers with Testing {

  "if 1" in {
    test(null,
         """
          |[{{ if true }}yes{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[yes]
        """.trim.stripMargin
  }

  "if 2" in {
    test(null,
         """
          |[{{ if true }}yes{{ else }}no{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[yes]
        """.trim.stripMargin
  }

  "if 3" in {
    test(null,
         """
          |[{{ if false }}yes{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[]
        """.trim.stripMargin
  }

  "if 4" in {
    test(null,
         """
          |[{{ if false }}yes{{ else }}no{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[no]
        """.trim.stripMargin
  }

  "if 5" in {
    testJson("3",
         """
          |[{{ if . = 3 }}three{{ elsif . = 4 }}four{{ else }}none{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[three]
        """.trim.stripMargin
  }

  "if 5a" in {
    testJson("4",
         """
          |[{{ if . = 3 }}three{{ elsif . = 4 }}four{{ else }}none{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[four]
        """.trim.stripMargin
  }

  "if 5b" in {
    testJson("5",
         """
          |[{{ if . = 3 }}three{{ elsif . = 4 }}four{{ else }}none{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[none]
        """.trim.stripMargin
  }

  "if 6" in {
    testJson("3",
         """
          |[{{ if . = 3 }}three{{ elsif . = 4 }}four{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[three]
        """.trim.stripMargin
  }

  "if 7" in {
    testJson("4",
         """
          |[{{ if . = 3 }}three{{ elsif . = 4 }}four{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[four]
        """.trim.stripMargin
  }

  "if 8" in {
    testJson("5",
         """
          |[{{ if . = 3 }}three{{ elsif . = 4 }}four{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[]
        """.trim.stripMargin
  }

  "if 9" in {
    testJson("3",
         """
          |[{{ if . = 3 }}three{{ elsif . = 4 }}four{{ elsif . = 5 }}five{{ else }}none{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[three]
        """.trim.stripMargin
  }

  "if 10" in {
    testJson("4",
         """
          |[{{ if . = 3 }}three{{ elsif . = 4 }}four{{ elsif . = 5 }}five{{ else }}none{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[four]
        """.trim.stripMargin
  }

  "if 11" in {
    testJson("5",
         """
          |[{{ if . = 3 }}three{{ elsif . = 4 }}four{{ elsif . = 5 }}five{{ else }}none{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[five]
        """.trim.stripMargin
  }

  "if 12" in {
    testJson("6",
         """
          |[{{ if . = 3 }}three{{ elsif . = 4 }}four{{ elsif . = 5 }}five{{ else }}none{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[none]
        """.trim.stripMargin
  }

  "if 13" in {
    testJson("3",
         """
          |[{{ if . = 3 }}three{{ elsif . = 4 }}four{{ elsif . = 5 }}five{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[three]
        """.trim.stripMargin
  }

  "if 14" in {
    testJson("4",
         """
          |[{{ if . = 3 }}three{{ elsif . = 4 }}four{{ elsif . = 5 }}five{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[four]
        """.trim.stripMargin
  }

  "if 15" in {
    testJson("5",
         """
          |[{{ if . = 3 }}three{{ elsif . = 4 }}four{{ elsif . = 5 }}five{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[five]
        """.trim.stripMargin
  }

  "if 16" in {
    testJson("6",
         """
          |[{{ if . = 3 }}three{{ elsif . = 4 }}four{{ elsif . = 5 }}five{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[]
        """.trim.stripMargin
  }

}
