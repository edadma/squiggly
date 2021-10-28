package io.github.edadma.squiggly

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class ArithmeticTests extends AnyFreeSpec with Matchers with Testing {

  "arithmetic 1" in {
    test(null, "{{ 3 + 4 }}") shouldBe "7"
  }

  "arithmetic 2" in {
    test(null, "{{ -3 + 4 }}") shouldBe "1"
  }

  "arithmetic 3" in {
    test(null, "{{ 3 + -4 }}") shouldBe "-1"
  }

  "arithmetic 4" in {
    test(null, "{{ -3 + -4 }}") shouldBe "-7"
  }

  "arithmetic 5" in {
    test(null, "{{ 3 * 4 }}") shouldBe "12"
  }

  "arithmetic 6" in {
    test(null, "{{ 3 / 4 }}") shouldBe "0.75"
  }

  "arithmetic 7" in {
    test(null, "{{ 10 \\ 4 }}") shouldBe "2"
  }

  "arithmetic 8" in {
    test(null, "{{ 7 mod 4 }}") shouldBe "3"
  }

  "arithmetic 9" in {
    test(null, "{{ 3 ^ 4 }}") shouldBe "81"
  }

  "arithmetic 10" in {
    test(null, "{{ 2 ^ 3 ^ 3 }}") shouldBe "134217728"
  }

  "arithmetic 11" in {
    test(null, "{{ 3 - 4 }}") shouldBe "-1"
  }

  "arithmetic 12" in {
    test(null, "{{ 3 + 4 * 5 }}") shouldBe "23"
  }

  "arithmetic 13" in {
    test(null, "{{ -3 + 4 * 5 }}") shouldBe "17"
  }

  "arithmetic 14" in {
    test(null, "{{ 3 + -4 * 5 }}") shouldBe "-17"
  }

  "arithmetic 15" in {
    test(null, "{{ 3 + 4 * -5 }}") shouldBe "-17"
  }

  "arithmetic 16" in {
    test(null, "{{ (3 + 4) * 5 }}") shouldBe "35"
  }

  "arithmetic 17" in {
    test(null, "{{ (-3 + 4) * 5 }}") shouldBe "5"
  }

  "arithmetic 18" in {
    test(null, "{{ (3 + -4) * 5 }}") shouldBe "-5"
  }

  "arithmetic 19" in {
    test(null, "{{ (3 + 4) * -5 }}") shouldBe "-35"
  }

  "arithmetic 20" in {
    test(null, "{{ -(3 + 4) * 5 }}") shouldBe "-35"
  }

}
