package io.github.edadma.squiggly

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class IndexTests extends AnyFreeSpec with Matchers with Testing {

  "index 1" in {
    test(
      "[3, 4, 5, 6]",
      "{{ .[3] }}"
    ) shouldBe "6"
  }

  "index 2" in {
    (the[RuntimeException] thrownBy test(
      "[3, 4, 5, 6]",
      "{{ .[-1] }}"
    )).getMessage should startWith("negative array index")
  }

  "index 3" in {
    (the[RuntimeException] thrownBy test(
      "[3, 4, 5, 6]",
      "{{ .[4] }}"
    )).getMessage should startWith("array index out of bounds")
  }

  "index 4" in {
    (the[RuntimeException] thrownBy test(
      "5",
      "{{ .[0] }}"
    )).getMessage should startWith("not indexable")
  }

  "index 5" in {
    test(
      "{a: 3, b: 4}",
      "{{ .['a'] }}"
    ) shouldBe "3"
  }

  "index 6" in {
    test(
      "{a: 3, b: 4}",
      "{{ .['x'] }}"
    ) shouldBe ""
  }

  "index 7" in {
    test(
      "'asdf'",
      "{{ .[3] }}"
    ) shouldBe "f"
  }

  "index 8" in {
    (the[RuntimeException] thrownBy test(
      "'asdf'",
      "{{ .[-1] }}"
    )).getMessage should startWith("negative array index")
  }

  "index 9" in {
    (the[RuntimeException] thrownBy test(
      "'asdf'",
      "{{ .[4] }}"
    )).getMessage should startWith("array index out of bounds")
  }

}
