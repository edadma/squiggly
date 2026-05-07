package io.github.edadma.squiggly

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

/** Coverage for `MethodExpr` at the renderer level — `<expr>.<id>` syntax.
  *
  * Two distinct paths converge at the same syntax:
  *   * On a `Map`, the dot accesses a key directly.
  *   * On any other value, `Context.lookup.tryMethod` falls through to the
  *     1-arity builtin registry and calls the matching function with the
  *     receiver as its sole argument (e.g. `'hi'.upper` → `upper('hi')`).
  *
  * `.foo.bar` (the *element-path* form starting with a leading dot at the
  * tag's outermost level) is parsed as `ElementExpr` rather than chained
  * `MethodExpr`, so those cases live in `VariableTests`/`LiteralsTests` and
  * aren't repeated here.
  */
class MethodCallTests extends AnyFreeSpec with Matchers with Testing {

  "method on a string" in {
    test(null, "{{ 'hello'.upper }}")  shouldBe "HELLO"
    test(null, "{{ 'WORLD'.lower }}")  shouldBe "world"
    test(null, "{{ 'hello'.length }}") shouldBe "5"
    test(null, "{{ 'hello'.reverse }}") shouldBe "olleh"
  }

  "method chain on a string" in {
    test(null, "{{ 'hello'.reverse.upper }}") shouldBe "OLLEH"
  }

  "method on a number" in {
    test(null, "{{ (-5).abs }}") shouldBe "5"
  }

  "method on a sequence" in {
    test(null, "{{ [10, 20, 30].head }}")     shouldBe "10"
    test(null, "{{ [10, 20, 30].last }}")     shouldBe "30"
    test(null, "{{ [10, 20, 30].length }}")   shouldBe "3"
    test(null, "{{ [1, 2, 3].reverse }}")     shouldBe "[3, 2, 1]"
    test(null, "{{ [1, 2, 3].sum }}")         shouldBe "6"
    test(null, "{{ [1, 2, 3].distinct }}")    shouldBe "[1, 2, 3]"
  }

  "method on a map literal accesses a key" in {
    test(null, "{{ {a: 1, b: 2}.a }}") shouldBe "1"
    test(null, "{{ {a: 1, b: 2}.b }}") shouldBe "2"
  }

  "method on a missing map key yields undefined (renders empty)" in {
    test(null, "[{{ {a: 1}.zzz }}]") shouldBe "[]"
  }

  "method on a bound variable" in {
    // Element-path syntax (.name.upper) parses as ElementExpr, not MethodExpr;
    // bind the value to a variable first to access the method-call path.
    test(null, "{{ s := 'hello' }}{{ s.upper }}") shouldBe "HELLO"
  }

  "method that doesn't apply errors" in {
    (the[RuntimeException] thrownBy
      test(null, "{{ 5.upper }}")).getMessage should startWith("cannot apply function 'upper'")
  }
}
