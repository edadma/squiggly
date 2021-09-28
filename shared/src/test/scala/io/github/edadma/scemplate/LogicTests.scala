package io.github.edadma.scemplate

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class LogicTests extends AnyFreeSpec with Matchers with Testing {

  "logic 1" in {
    test(null, "{{ true and true }}") shouldBe "true"
  }

  "logic 2" in {
    test(null, "{{ -3 + 4 }}") shouldBe "1"
  }

  "logic 3" in {
    test(null, "{{ 3 + -4 }}") shouldBe "-1"
  }

  "logic 4" in {
    test(null, "{{ -3 + -4 }}") shouldBe "-7"
  }

  "logic 5" in {
    test(null, "{{ 3 * 4 }}") shouldBe "12"
  }

  "logic 6" in {
    test(null, "{{ 3 / 4 }}") shouldBe "0.75"
  }

  "logic 7" in {
    test(null, "{{ 10 \\ 4 }}") shouldBe "2"
  }

  "logic 8" in {
    test(null, "{{ 7 mod 4 }}") shouldBe "3"
  }

  "logic 9" in {
    test(null, "{{ 3 ^ 4 }}") shouldBe "81"
  }

  "logic 10" in {
    test(null, "{{ 2 ^ 3 ^ 3 }}") shouldBe "134217728"
  }

  "logic 11" in {
    test(null, "{{ 3 - 4 }}") shouldBe "-1"
  }

  "logic 12" in {
    test(null, "{{ 3 + 4 * 5 }}") shouldBe "23"
  }

  "logic 13" in {
    test(null, "{{ -3 + 4 * 5 }}") shouldBe "17"
  }

  "logic 14" in {
    test(null, "{{ 3 + -4 * 5 }}") shouldBe "-17"
  }

  "logic 15" in {
    test(null, "{{ 3 + 4 * -5 }}") shouldBe "-17"
  }

  "logic 16" in {
    test(null, "{{ (3 + 4) * 5 }}") shouldBe "35"
  }

  "logic 17" in {
    test(null, "{{ (-3 + 4) * 5 }}") shouldBe "5"
  }

  "logic 18" in {
    test(null, "{{ (3 + -4) * 5 }}") shouldBe "-5"
  }

  "logic 19" in {
    test(null, "{{ (3 + 4) * -5 }}") shouldBe "-35"
  }

  "logic 20" in {
    test(null, "{{ -(3 + 4) * 5 }}") shouldBe "-35"
  }

}
