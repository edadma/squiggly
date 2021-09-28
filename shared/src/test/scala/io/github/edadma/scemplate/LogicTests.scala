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
    test(null, "{{ true or true and false }}") shouldBe "true"
  }

  "logic 11" in {
    test(null, "{{ (true or true) and false }}") shouldBe "false"
  }

  "logic 12" in {
    test(null, "{{ not true }}") shouldBe "false"
  }

  "logic 13" in {
    test(null, "{{ not false }}") shouldBe "true"
  }

  "logic 14" in {
    test(null, "{{ not 3 < 4 }}") shouldBe "false"
  }

  "logic 15" in {
    test(null, "{{ not 4 < 3 }}") shouldBe "true"
  }

  "logic 16" in {
    test(null, "{{ 3 < 4 or 3 < 4 and 4 < 3 }}") shouldBe "true"
  }

}
