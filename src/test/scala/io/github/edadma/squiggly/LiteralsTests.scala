package io.github.edadma.squiggly

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class LiteralsTests extends AnyFreeSpec with Matchers with Testing {

  "string literals" in {
    test(
      null,
      """
        |strings {{ 'asdf' }}, {{ "asdf" }}, {{ 'as\tdf' }}, {{ 'as\u03B1df' }}, {{ 'as\'df' }}
        """.trim.stripMargin,
    ) shouldBe
      s"""
        |strings asdf, asdf, as\tdf, as\u03B1df, as\'df
        """.trim.stripMargin
  }

  "null literals" in {
    test(
      null,
      """
        |nulls {{ null }} are blank
        """.trim.stripMargin,
    ) shouldBe
      """
        |nulls  are blank
        """.trim.stripMargin
  }

  "map literals" in {
    test(
      null,
      """
        |maps {{ {} }} {{ { a :123 } }} {{ {a: 3, b: 4} }} {{ {a: 123}.a }} {{ {a: 123}.asdf }} (undefined)
        """.trim.stripMargin,
    ) shouldBe
      """
        |maps {} {a: 123} {a: 3, b: 4} 123  (undefined)
        """.trim.stripMargin
  }

  "sequence literals" in {
    test(
      null,
      """
        |sequences {{ [] }} {{ [ 123 ] }} {{ [3, 4] }} {{ [123][0] }}
        """.trim.stripMargin,
    ) shouldBe
      """
        |sequences [] [123] [3, 4] 123
        """.trim.stripMargin
  }

  "boolean literals" in {
    test(
      "{t: true, f: false}",
      """
        |booleans {{ .t }}, {{ .f }}, {{ true }}, {{ false }}
        """.trim.stripMargin,
    ) shouldBe
      """
        |booleans true, false, true, false
        """.trim.stripMargin
  }

  "integer literals" in {
    test(
      "{a: -345, b: 0, c: 345}",
      """
        |ints {{ .a }} {{ .b }} {{ .c }} {{ -345 }} {{ 0 }} {{ 345 }}
        """.trim.stripMargin,
    ) shouldBe
      """
        |ints -345 0 345 -345 0 345
        """.trim.stripMargin
  }

  "decimal literals" in {
    test(
      "{N: 6.02214076e23}",
      """
        |Avogadro number {{ .N }} {{ 6.02214076e23 }}
        """.trim.stripMargin,
    ) shouldBe
      """
        |Avogadro number 6.02214076E+23 6.02214076E+23
        """.trim.stripMargin
  }

}
