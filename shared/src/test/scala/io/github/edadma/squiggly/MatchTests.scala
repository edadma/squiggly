package io.github.edadma.squiggly

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class MatchTests extends AnyFreeSpec with Matchers with Testing {

  "match 1" in {
    test("3",
         """
          |[{{ match . }}{{ case 3 }}three{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[three]
        """.trim.stripMargin
  }

  "match 2" in {
    test(null,
         """
          |[{{ match 3 }}{{ case 3 }}three{{ else }}no{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[three]
        """.trim.stripMargin
  }

  "match 3" in {
    test(null,
         """
          |[{{ match 4 }}{{ case 3 }}three{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[]
        """.trim.stripMargin
  }

  "match 4" in {
    test(null,
         """
          |[{{ match 4 }}{{ case 3 }}three{{ else }}no{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[no]
        """.trim.stripMargin
  }

  "match 5" in {
    test(null,
         """
          |[{{ match 3 }}{{ case 3 }}three{{ case 4 }}four{{ else }}none{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[three]
        """.trim.stripMargin
  }

  "match 5a" in {
    test("4",
         """
          |[{{ match . }}{{ case 3 }}three{{ case 4 }}four{{ else }}none{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[four]
        """.trim.stripMargin
  }

  "match 5b" in {
    test("5",
         """
          |[{{ match . }}{{ case 3 }}three{{ case 4 }}four{{ else }}none{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[none]
        """.trim.stripMargin
  }

  "match 6" in {
    test("3",
         """
          |[{{ match . }}{{ case 3 }}three{{ case 4 }}four{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[three]
        """.trim.stripMargin
  }

  "match 7" in {
    test("4",
         """
          |[{{ match . }}{{ case 3 }}three{{ case 4 }}four{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[four]
        """.trim.stripMargin
  }

  "match 8" in {
    test("5",
         """
          |[{{ match . }}{{ case 3 }}three{{ case4 }}four{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[]
        """.trim.stripMargin
  }

  "match 9" in {
    test("3",
         """
          |[{{ match . }}{{ case 3 }}three{{ case 4 }}four{{ case 5 }}five{{ else }}none{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[three]
        """.trim.stripMargin
  }

  "match 10" in {
    test("4",
         """
          |[{{ match . }}{{ case 3 }}three{{ case 4 }}four{{ case 5 }}five{{ else }}none{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[four]
        """.trim.stripMargin
  }

  "match 11" in {
    test("5",
         """
          |[{{ match . }}{{ case 3 }}three{{ case 4 }}four{{ case 5 }}five{{ else }}none{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[five]
        """.trim.stripMargin
  }

  "match 12" in {
    test("6",
         """
          |[{{ match . }}{{ case 3 }}three{{ case 4 }}four{{ case 5 }}five{{ else }}none{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[none]
        """.trim.stripMargin
  }

  "match 13" in {
    test("3",
         """
          |[{{ match . }}{{ case 3 }}three{{ case 4 }}four{{ case 5 }}five{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[three]
        """.trim.stripMargin
  }

  "match 14" in {
    test("4",
         """
          |[{{ match . }}{{ case 3 }}three{{ case 4 }}four{{ case 5 }}five{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[four]
        """.trim.stripMargin
  }

  "match 15" in {
    test("5",
         """
          |[{{match . }}{{ case 3 }}three{{ case 4 }}four{{ case 5 }}five{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[five]
        """.trim.stripMargin
  }

  "match 16" in {
    test("6",
         """
          |[{{ match . }}{{ case 3 }}three{{ case 4 }}four{{ case 5 }}five{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[]
        """.trim.stripMargin
  }

}
