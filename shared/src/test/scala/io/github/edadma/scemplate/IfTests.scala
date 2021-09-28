package io.github.edadma.scemplate

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
    test("3",
         """
          |[{{ if . = 3 }}three{{ else if . = 4 }}four{{ else }}none{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[three]
        """.trim.stripMargin
  }

  "if 6" in {
    test("3",
         """
          |[{{ if . = 3 }}three{{ else if . = 4 }}four{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[three]
        """.trim.stripMargin
  }

  "if 7" in {
    test("4",
         """
          |[{{ if . = 3 }}three{{ else if . = 4 }}four{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[four]
        """.trim.stripMargin
  }

  "if 8" in {
    test("5",
         """
          |[{{ if . = 3 }}three{{ else if . = 4 }}four{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[]
        """.trim.stripMargin
  }

}
