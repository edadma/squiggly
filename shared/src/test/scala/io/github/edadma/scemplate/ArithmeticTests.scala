package io.github.edadma.scemplate

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

}
