package io.github.edadma.scemplate

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class VariableTests extends AnyFreeSpec with Matchers with Testing {

  "vars 1" in {
    test("{a: {b: 3, c: {d: 4}}}",
         """
        |
        |{{ .a.b }} {{ .a.c.d }} {{ with .a }}
        """.trim.stripMargin) shouldBe
      """
        |
        |3 4
        """.trim.stripMargin
  }

}
