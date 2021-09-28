package io.github.edadma.scemplate

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class LogicTests extends AnyFreeSpec with Matchers with Testing {

  "logic 1" in {
    test(null, "{{ true and true }}") shouldBe "true"
  }

  "logic 2" in {
    test(null, "{{ true and false }}") shouldBe "false"
  }

  "logic 3" in {
    test(null, "{{ false and true }}") shouldBe "false"
  }

  "logic 4" in {
    test(null, "{{ false and false }}") shouldBe "false"
  }

  "logic 5" in {
    test(null, "{{ false or false }}") shouldBe "false"
  }

  "logic 6" in {
    test(null, "{{ false or true }}") shouldBe "true"
  }

  "logic 7" in {
    test(null, "{{ true or true }}") shouldBe "true"
  }

  "logic 8" in {
    test(null, "{{ true or false }}") shouldBe "true"
  }

  "logic 9" in {
    test(null, "{{ true or true and true }}") shouldBe "true"
  }

  "logic 10" in {
    test(null, "{{true or true and false }}") shouldBe "true"
  }

  "logic 11" in {
    test(null, "{{(true or true) and false }}") shouldBe "false"
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
