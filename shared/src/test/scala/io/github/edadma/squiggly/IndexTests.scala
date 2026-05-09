package io.github.edadma.squiggly

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class IndexTests extends AnyFreeSpec with Matchers with Testing {

  "index 1" in {
    testJson(
      "[3, 4, 5, 6]",
      "{{ .[3] }}",
    ) shouldBe "6"
  }

  "index 2" in {
    (the[RuntimeException] thrownBy testJson(
      "[3, 4, 5, 6]",
      "{{ .[-1] }}",
    )).getMessage should startWith("negative array index")
  }

  "index 3" in {
    (the[RuntimeException] thrownBy testJson(
      "[3, 4, 5, 6]",
      "{{ .[4] }}",
    )).getMessage should startWith("array index out of bounds")
  }

  "index 4" in {
    (the[RuntimeException] thrownBy testJson(
      "5",
      "{{ .[0] }}",
    )).getMessage should startWith("not indexable")
  }

  "index 5" in {
    testJson(
      """{"a": 3, "b": 4}""",
      "{{ .['a'] }}",
    ) shouldBe "3"
  }

  "index 6" in {
    testJson(
      """{"a": 3, "b": 4}""",
      "{{ .['x'] }}",
    ) shouldBe ""
  }

  "index 7" in {
    testJson(
      "\"asdf\"",
      "{{ .[3] }}",
    ) shouldBe "f"
  }

  "index 8" in {
    (the[RuntimeException] thrownBy testJson(
      "\"asdf\"",
      "{{ .[-1] }}",
    )).getMessage should startWith("negative array index")
  }

  "index 9" in {
    (the[RuntimeException] thrownBy testJson(
      "\"asdf\"",
      "{{ .[4] }}",
    )).getMessage should startWith("array index out of bounds")
  }

  // Subscript on a parenthesised expression. Paren-grouping forces the
  // grammar through `primary → ( expression )`, so the `index` rule
  // can chain `[idx]` onto the result. Bare-variable subscript
  // (`xs[0]` without parens) currently doesn't work — see the
  // `KNOWN LIMITATION` comment on `TagParser.apply`.
  "subscript on parenthesised assignment" in {
    testJson(
      "[7, 8, 9]",
      "{{ ys := . }}{{ (ys)[1] }}",
    ) shouldBe "8"
  }

  "function-not-found error message" in {
    // The old message was misleading: `function found: foo` actually
    // meant `foo is NOT a registered function and you tried to call it
    // with arguments`. Renamed to `function not found` so the diagnostic
    // matches the meaning.
    (the[RuntimeException] thrownBy testJson(
      "0",
      "{{ nosuchfunction 1 }}",
    )).getMessage should startWith("function not found")
  }

}
