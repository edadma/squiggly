package io.github.edadma.squiggly

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class MatchTests extends AnyFreeSpec with Matchers with Testing {

  "match 1" in {
    testJson("3",
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
    testJson("4",
         """
          |[{{ match . }}{{ case 3 }}three{{ case 4 }}four{{ else }}none{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[four]
        """.trim.stripMargin
  }

  "match 5b" in {
    testJson("5",
         """
          |[{{ match . }}{{ case 3 }}three{{ case 4 }}four{{ else }}none{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[none]
        """.trim.stripMargin
  }

  "match 6" in {
    testJson("3",
         """
          |[{{ match . }}{{ case 3 }}three{{ case 4 }}four{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[three]
        """.trim.stripMargin
  }

  "match 7" in {
    testJson("4",
         """
          |[{{ match . }}{{ case 3 }}three{{ case 4 }}four{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[four]
        """.trim.stripMargin
  }

  "match 8" in {
    testJson("5",
         """
          |[{{ match . }}{{ case 3 }}three{{ case 4 }}four{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[]
        """.trim.stripMargin
  }

  "match 9" in {
    testJson("3",
         """
          |[{{ match . }}{{ case 3 }}three{{ case 4 }}four{{ case 5 }}five{{ else }}none{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[three]
        """.trim.stripMargin
  }

  "match 10" in {
    testJson("4",
         """
          |[{{ match . }}{{ case 3 }}three{{ case 4 }}four{{ case 5 }}five{{ else }}none{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[four]
        """.trim.stripMargin
  }

  "match 11" in {
    testJson("5",
         """
          |[{{ match . }}{{ case 3 }}three{{ case 4 }}four{{ case 5 }}five{{ else }}none{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[five]
        """.trim.stripMargin
  }

  "match 12" in {
    testJson("6",
         """
          |[{{ match . }}{{ case 3 }}three{{ case 4 }}four{{ case 5 }}five{{ else }}none{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[none]
        """.trim.stripMargin
  }

  "match 13" in {
    testJson("3",
         """
          |[{{ match . }}{{ case 3 }}three{{ case 4 }}four{{ case 5 }}five{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[three]
        """.trim.stripMargin
  }

  "match 14" in {
    testJson("4",
         """
          |[{{ match . }}{{ case 3 }}three{{ case 4 }}four{{ case 5 }}five{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[four]
        """.trim.stripMargin
  }

  "match 15" in {
    testJson("5",
         """
          |[{{match . }}{{ case 3 }}three{{ case 4 }}four{{ case 5 }}five{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[five]
        """.trim.stripMargin
  }

  "match 16" in {
    testJson("6",
         """
          |[{{ match . }}{{ case 3 }}three{{ case 4 }}four{{ case 5 }}five{{ end }}]
          """.trim.stripMargin) shouldBe
      """
        |[]
        """.trim.stripMargin
  }

}
