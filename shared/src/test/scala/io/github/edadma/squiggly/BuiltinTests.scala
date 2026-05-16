package io.github.edadma.squiggly

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

/** Renderer-level coverage of every template builtin. One representative test
  * per function, focused on the happy path. Side-effect-only builtins
  * (`print`, `println`, `context`), non-deterministic ones (`random`,
  * `shuffle`), and the filesystem-dependent `partial` are intentionally
  * excluded — they belong with their own targeted suites.
  */
class BuiltinTests extends AnyFreeSpec with Matchers with Testing {

  // ------------------------------------------------------------------ numeric

  "abs" in {
    test(null, "{{ abs -5 }}") shouldBe "5"
    test(null, "{{ abs 5 }}")  shouldBe "5"
  }

  "ceil" in {
    test(null, "{{ ceil 3.2 }}") shouldBe "4"
    test(null, "{{ ceil 3 }}")   shouldBe "3"
  }

  "floor" in {
    test(null, "{{ floor 3.9 }}") shouldBe "3"
    test(null, "{{ floor 3 }}")   shouldBe "3"
  }

  "round" in {
    test(null, "{{ round 3.5 }}")  shouldBe "4"
    test(null, "{{ round 2.5 }}")  shouldBe "2" // half-even
    test(null, "{{ round -3.5 }}") shouldBe "-4"
  }

  "max" in {
    test(null, "{{ max 3 7 }}") shouldBe "7"
    test(null, "{{ max 9 1 }}") shouldBe "9"
  }

  "min" in {
    test(null, "{{ min 3 7 }}") shouldBe "3"
    test(null, "{{ min 9 1 }}") shouldBe "1"
  }

  "number" in {
    test(null, "{{ number '42' }}") shouldBe "42"
  }

  "sum" in {
    test(null, "{{ sum [1, 2, 3, 4] }}") shouldBe "10"
  }

  // ----------------------------------------------------------------- strings

  "capitalize" in {
    test(null, "{{ capitalize 'hello WORLD' }}") shouldBe "Hello world"
    test(null, "{{ capitalize '' }}")            shouldBe ""
  }

  "lower" in {
    test(null, "{{ lower 'HELLO' }}") shouldBe "hello"
  }

  "upper" in {
    test(null, "{{ upper 'hello' }}") shouldBe "HELLO"
  }

  "htmlEscape" in {
    test(null, """{{ htmlEscape '<a href="x">x&y</a>' }}""") shouldBe
      """&lt;a href=&quot;x&quot;&gt;x&amp;y&lt;/a&gt;"""
  }

  "newline_to_br" in {
    test(null, """{{ newline_to_br "a\nb" }}""") shouldBe "a<br />\nb"
  }

  "ltrim" in {
    test(null, "{{ ltrim '   hello   ' }}") shouldBe "hello   "
  }

  "rtrim" in {
    test(null, "{{ rtrim '   hello   ' }}") shouldBe "   hello"
  }

  "trim" in {
    test(null, "{{ trim '   hello   ' }}") shouldBe "hello"
  }

  "remove" in {
    test(null, "{{ remove 'l' 'hello' }}") shouldBe "heo"
  }

  "removeFirst" in {
    test(null, "{{ removeFirst 'l' 'hello' }}") shouldBe "helo"
  }

  "urlize" in {
    test(null, "{{ urlize 'Hello, World!' }}") shouldBe "Hello-World"
    test(null, "{{ urlize '' }}")               shouldBe "-"
  }

  "slugify" in {
    test(null, "{{ slugify 'Hello, World!' }}") shouldBe "hello-world"
    test(null, "{{ slugify 'Café au lait' }}")  shouldBe "cafe-au-lait"
    test(null, "{{ slugify 'C++' }}")           shouldBe "c"
    test(null, "{{ slugify '___' }}")           shouldBe "-"
  }

  "jsonStr escapes JSON special chars" in {
    test(null, "{{ jsonStr 'he said \"hi\"' }}") shouldBe """he said \"hi\""""
    test(null, "{{ jsonStr 'a\\nb' }}")          shouldBe "a\\nb"
  }

  "emojify substitutes shortcodes" in {
    // The exact unicode depends on the emoji table; the contract is
    // that recognized shortcodes get replaced and unrecognized ones
    // pass through. ":smile:" maps to U+1F604 in the standard table.
    val out = test(null, "{{ emojify ':smile: hi' }}")
    out should not include ":smile:"
    out should include("hi")
  }

  "markdownify renders bold + paragraphs" in {
    val out = test(null, "{{ markdownify 'A **bold** word.' }}")
    out should include("<strong>bold</strong>")
    out should include("<p>")
  }

  "urlEncode" in {
    test(null, "{{ urlEncode 'a b/c' }}") shouldBe "a+b%2Fc"
  }

  "urlDecode" in {
    test(null, "{{ urlDecode 'a+b%2Fc' }}") shouldBe "a b/c"
  }

  "length on string" in {
    test(null, "{{ length 'hello' }}") shouldBe "5"
  }

  "length on seq" in {
    test(null, "{{ length [1, 2, 3] }}") shouldBe "3"
  }

  "isEmpty true" in {
    test(null, "{{ isEmpty '' }}") shouldBe "true"
    test(null, "{{ isEmpty [] }}") shouldBe "true"
  }

  "isEmpty false" in {
    test(null, "{{ isEmpty 'hi' }}")    shouldBe "false"
    test(null, "{{ isEmpty [1, 2] }}")  shouldBe "false"
  }

  "nonEmpty" in {
    test(null, "{{ nonEmpty 'hi' }}")  shouldBe "true"
    test(null, "{{ nonEmpty '' }}")    shouldBe "false"
  }

  "split" in {
    test(null, "{{ split ',' 'a,b,c' }}") shouldBe "[a, b, c]"
  }

  "startsWith" in {
    test(null, "{{ startsWith 'he' 'hello' }}") shouldBe "true"
    test(null, "{{ startsWith 'xy' 'hello' }}") shouldBe "false"
  }

  "substring" in {
    test(null, "{{ substring 1 4 'abcdef' }}") shouldBe "bcd"
  }

  "truncate (default ellipsis)" in {
    test(null, "{{ truncate 2 'one two three four' }}") shouldBe "one two..."
  }

  "truncate (custom ellipsis)" in {
    test(null, "{{ truncate 2 'one two three four' '…' }}") shouldBe "one two…"
  }

  // ----------------------------------------------------------- collection ops

  "head" in {
    test(null, "{{ head [10, 20, 30] }}") shouldBe "10"
  }

  "last" in {
    test(null, "{{ last [10, 20, 30] }}") shouldBe "30"
  }

  "tail" in {
    test(null, "{{ tail [10, 20, 30] }}") shouldBe "[20, 30]"
  }

  "append" in {
    test(null, "{{ append 4 [1, 2, 3] }}") shouldBe "[1, 2, 3, 4]"
  }

  "prepend" in {
    test(null, "{{ prepend 0 [1, 2, 3] }}") shouldBe "[0, 1, 2, 3]"
  }

  "reverse seq" in {
    test(null, "{{ reverse [1, 2, 3] }}") shouldBe "[3, 2, 1]"
  }

  "reverse string" in {
    test(null, "{{ reverse 'abc' }}") shouldBe "cba"
  }

  "distinct" in {
    test(null, "{{ distinct [1, 2, 2, 3, 1] }}") shouldBe "[1, 2, 3]"
  }

  "compact" in {
    testJson("[1, null, 2, null, 3]", "{{ compact . }}") shouldBe "[1, 2, 3]"
  }

  "drop" in {
    test(null, "{{ drop 2 [1, 2, 3, 4] }}") shouldBe "[3, 4]"
  }

  "dropRight" in {
    test(null, "{{ dropRight 2 [1, 2, 3, 4] }}") shouldBe "[1, 2]"
  }

  "take" in {
    test(null, "{{ take 2 [1, 2, 3, 4] }}") shouldBe "[1, 2]"
  }

  "takeRight" in {
    test(null, "{{ takeRight 2 [1, 2, 3, 4] }}") shouldBe "[3, 4]"
  }

  "slice (from)" in {
    test(null, "{{ slice 1 [10, 20, 30, 40] }}") shouldBe "[20, 30, 40]"
  }

  "slice (from, until)" in {
    test(null, "{{ slice 1 3 [10, 20, 30, 40] }}") shouldBe "[20, 30]"
  }

  "join (delim)" in {
    test(null, "{{ join '-' [1, 2, 3] }}") shouldBe "1-2-3"
  }

  "contains in seq" in {
    test(null, "{{ contains 2 [1, 2, 3] }}") shouldBe "true"
    test(null, "{{ contains 9 [1, 2, 3] }}") shouldBe "false"
  }

  "contains substring" in {
    test(null, "{{ contains 'ell' 'hello' }}") shouldBe "true"
  }

  "contains in map" in {
    testJson("""{"a": 1, "b": 2}""", "{{ contains 'a' . }}") shouldBe "true"
    testJson("""{"a": 1, "b": 2}""", "{{ contains 'z' . }}") shouldBe "false"
  }

  "toSeq" in {
    test(null, "{{ toSeq [1, 2, 3] }}") shouldBe "[1, 2, 3]"
  }

  "toString" in {
    test(null, "{{ toString 42 }}")    shouldBe "42"
    test(null, "{{ toString true }}")  shouldBe "true"
  }

  // -------------------------------------------------------------- set ops

  "intersect" in {
    test(null, "{{ intersect [1, 2, 3] [2, 3, 4] }}") shouldBe "[2, 3]"
  }

  "union" in {
    test(null, "{{ union [1, 2] [2, 3] }}") shouldBe "[1, 2, 3]"
  }

  "symdiff" in {
    test(null, "{{ symdiff [1, 2, 3] [2, 3, 4] }}") shouldBe "[1, 4]"
  }

  "complement" in {
    test(null, "{{ complement [1, 2] [2, 3, 4] }}") shouldBe "[3, 4]"
  }

  // ---------------------------------------------------------- higher-order

  "filterNot" in {
    testJson(
      "[1, 2, 3, 4]",
      "{{ filterNot `. > 2` . }}",
    ) shouldBe "[1, 2]"
  }

  "map" in {
    testJson(
      "[1, 2, 3]",
      "{{ map `. * 2` . }}",
    ) shouldBe "[2, 4, 6]"
  }

  // -------------------------------------------------------------- regex

  "findRE (no groups)" in {
    // Each match is a list of [fullMatch, group1, group2, ...] — no groups
    // means each entry is a one-element list with just the match.
    test(null, """{{ findRE '[a-z]+' 'hello 123 world' }}""") shouldBe "[[hello], [world]]"
  }

  "findRE (with groups)" in {
    test(null, """{{ findRE '<p>(.+?)</p>' '<p>one</p><p>two</p>' }}""") shouldBe
      "[[<p>one</p>, one], [<p>two</p>, two]]"
  }

  "findRE indexed access" in {
    test(null, """{{ (findRE '<p>(.+?)</p>' '<p>hello</p>')[0][1] }}""") shouldBe "hello"
  }

  "findRE (limited)" in {
    test(null, """{{ findRE '[a-z]+' 'a b c d' 2 }}""") shouldBe "[[a], [b]]"
  }

  // ------------------------------------------------------------ misc

  "querify" in {
    testJson("""{"a": 1, "b": 2}""", "{{ querify . }}") shouldBe "a=1&b=2"
  }

  "default (used)" in {
    test(null, "{{ default 'fallback' '' }}") shouldBe "fallback"
  }

  "default (passthrough)" in {
    test(null, "{{ default 'fallback' 'real' }}") shouldBe "real"
  }

  // ------------------------------------------------------- filesystem

  "fileExists negative" in {
    test(null, "{{ fileExists '/__definitely_not_a_real_path__' }}") shouldBe "false"
  }

}
