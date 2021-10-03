package io.github.edadma.squiggly

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class VariableTests extends AnyFreeSpec with Matchers with Testing {

  "vars 1" in {
    test("{a: {b: 3, c: {d: 4}}}",
         """
        |{{ .a.b }} {{ .a.c.d }} {{ with .a }}{{ .b }} {{ .c.d }}{{ end }}
        """.trim.stripMargin) shouldBe
      """
        |3 4 3 4
        """.trim.stripMargin
  }

  "vars 2" in {
    test(null,
         """
        |{{ v := 345 }}{{ v }} {{ v := 678 }}{{ v }}
        """.trim.stripMargin) shouldBe
      """
        |345 678
        """.trim.stripMargin
  }

  "vars 3" in {
    (the[RuntimeException] thrownBy test(null,
                                         """
        |{{ v := 345 }}{{ asdf }}
        """.trim.stripMargin)).getMessage should startWith("unknown variable: asdf")
  }

  "vars 4" in {
    test("{a: {b: 3, c: {d: 4}}}",
         """
        |[{{ .a.d }}]
        """.trim.stripMargin) shouldBe
      """
        |[]
        """.trim.stripMargin
  }

}