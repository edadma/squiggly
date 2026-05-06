package io.github.edadma.squiggly

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

/** Direct unit tests for the combinator-based [[TagParser]].
  *
  * These exercise only the parser — no template tokenizer, no renderer. Each
  * test parses a tag-interior string (i.e. the bit between `{{ }}`) and asserts
  * the resulting [[TagParserAST]] structurally. AST nodes extend
  * `scala.util.parsing.input.Positional`, but case-class equality only
  * considers constructor fields, so position information is invisible to
  * `shouldBe` comparisons.
  */
class TagParserTests extends AnyFreeSpec with Matchers {

  private def p(s: String): TagParserAST = TagParser.parse(s)

  // Convenience constructors — strip noise from expected ASTs.
  private def num(n: Int): NumberExpr             = NumberExpr(BigDecimal(n))
  private def num(n: BigDecimal): NumberExpr      = NumberExpr(n)
  private def str(s: String): StringExpr          = StringExpr(s)
  private def varExpr(n: String): VarExpr         = VarExpr("", Ident(n))
  private def gvar(n: String): VarExpr            = VarExpr("$", Ident(n))
  private def elem(names: String*): ElementExpr   = ElementExpr("", names.map(Ident.apply).toList)
  private def gelem(names: String*): ElementExpr  = ElementExpr("$", names.map(Ident.apply).toList)

  "literals" - {
    "integer" in {
      p("5") shouldBe num(5)
    }
    "negative integer" in {
      p("-5") shouldBe PrefixExpr("-", num(5))
    }
    "decimal" in {
      p("3.14") shouldBe num(BigDecimal("3.14"))
    }
    "scientific" in {
      p("6.02e23") shouldBe num(BigDecimal("6.02e23"))
    }
    "true" in {
      p("true") shouldBe BooleanExpr(true)
    }
    "false" in {
      p("false") shouldBe BooleanExpr(false)
    }
    "null" in {
      p("null") shouldBe NullExpr()
    }
    "single-quoted string" in {
      p("'hello'") shouldBe str("hello")
    }
    "double-quoted string" in {
      p("\"hello\"") shouldBe str("hello")
    }
    "string with escape" in {
      p("'a\\tb'") shouldBe str("a\tb")
    }
  }

  "variable and element access" - {
    "bare ident is a VarExpr" in {
      p("x") shouldBe varExpr("x")
    }
    "global var" in {
      p("$x") shouldBe gvar("x")
    }
    "single-component element path" in {
      p(".a") shouldBe elem("a")
    }
    "nested element path" in {
      p(".a.b.c") shouldBe elem("a", "b", "c")
    }
    "global element path" in {
      p("$.a.b") shouldBe gelem("a", "b")
    }
  }

  "arithmetic" - {
    "addition" in {
      p("3 + 4") shouldBe LeftInfixExpr(num(3), "+", num(4))
    }
    "subtraction" in {
      p("3 - 4") shouldBe LeftInfixExpr(num(3), "-", num(4))
    }
    "left-associative chain" in {
      p("3 + 4 - 1") shouldBe
        LeftInfixExpr(LeftInfixExpr(num(3), "+", num(4)), "-", num(1))
    }
    "precedence: * binds tighter than +" in {
      p("3 + 4 * 5") shouldBe
        LeftInfixExpr(num(3), "+", LeftInfixExpr(num(4), "*", num(5)))
    }
    "parentheses override precedence" in {
      p("(3 + 4) * 5") shouldBe
        LeftInfixExpr(LeftInfixExpr(num(3), "+", num(4)), "*", num(5))
    }
    "unary minus on parenthesised expression" in {
      p("-(3 + 4)") shouldBe PrefixExpr("-", LeftInfixExpr(num(3), "+", num(4)))
    }
    "exponentiation is right-associative" in {
      p("2 ^ 3 ^ 2") shouldBe
        RightInfixExpr(num(2), "^", RightInfixExpr(num(3), "^", num(2)))
    }
    "modulus" in {
      p("7 mod 3") shouldBe LeftInfixExpr(num(7), "mod", num(3))
    }
    "integer division" in {
      p("10 \\ 3") shouldBe LeftInfixExpr(num(10), "\\", num(3))
    }
    "string concat ++" in {
      p("'a' ++ 'b'") shouldBe LeftInfixExpr(str("a"), "++", str("b"))
    }
  }

  "logic" - {
    "not" in {
      p("not x") shouldBe PrefixExpr("not", varExpr("x"))
    }
    "and" in {
      p("x and y") shouldBe AndExpr(varExpr("x"), varExpr("y"))
    }
    "or" in {
      p("x or y") shouldBe OrExpr(varExpr("x"), varExpr("y"))
    }
    "and binds tighter than or" in {
      p("a or b and c") shouldBe
        OrExpr(varExpr("a"), AndExpr(varExpr("b"), varExpr("c")))
    }
  }

  "comparison" - {
    "less-than" in {
      p("a < b") shouldBe CompareExpr(varExpr("a"), List(("<", varExpr("b"))))
    }
    "chained" in {
      p("a < b <= c") shouldBe
        CompareExpr(varExpr("a"), List(("<", varExpr("b")), ("<=", varExpr("c"))))
    }
    "equality" in {
      p("a = b") shouldBe CompareExpr(varExpr("a"), List(("=", varExpr("b"))))
    }
    "not-equal" in {
      p("a != b") shouldBe CompareExpr(varExpr("a"), List(("!=", varExpr("b"))))
    }
    "div predicate" in {
      p("a div b") shouldBe CompareExpr(varExpr("a"), List(("div", varExpr("b"))))
    }
  }

  "conditional expressions" - {
    "if-then" in {
      p("if x then 1") shouldBe ConditionalAST(varExpr("x"), num(1), None)
    }
    "if-then-else" in {
      p("if x then 1 else 2") shouldBe
        ConditionalAST(varExpr("x"), num(1), Some(num(2)))
    }
  }

  "map and seq literals" - {
    "empty map" in {
      p("{}") shouldBe MapExpr(Nil)
    }
    "two-entry map" in {
      p("{a: 1, b: 2}") shouldBe
        MapExpr(List((Ident("a"), num(1)), (Ident("b"), num(2))))
    }
    "empty seq" in {
      p("[]") shouldBe SeqExpr(Nil)
    }
    "three-element seq" in {
      p("[1, 2, 3]") shouldBe SeqExpr(List(num(1), num(2), num(3)))
    }
  }

  "index and method access" - {
    "index of element path" in {
      p(".a[0]") shouldBe IndexExpr(elem("a"), num(0))
    }
    "method on a number" in {
      p("5.upper") shouldBe MethodExpr(num(5), Ident("upper"))
    }
  }

  "function application" - {
    "single argument" in {
      p("upper 'hello'") shouldBe ApplyExpr(Ident("upper"), List(str("hello")))
    }
    "multiple arguments" in {
      p("substring 0 5 'helloworld'") shouldBe
        ApplyExpr(Ident("substring"), List(num(0), num(5), str("helloworld")))
    }
  }

  "pipe" - {
    "value piped to a no-arg call" in {
      p("'hello' | upper") shouldBe
        PipeExpr(str("hello"), ApplyExpr(Ident("upper"), Nil))
    }
  }

  "tags" - {
    "if (block-start, no `then`)" in {
      p("if x") shouldBe IfAST(varExpr("x"))
    }
    "elsif" in {
      p("elsif x") shouldBe ElseIfAST(varExpr("x"))
    }
    "else" in {
      p("else") shouldBe ElseAST()
    }
    "end" in {
      p("end") shouldBe EndAST()
    }
    "for over an element" in {
      p("for .items") shouldBe ForAST(None, elem("items"))
    }
    "for with element binding" in {
      p("for x <- .items") shouldBe
        ForAST(Some((Ident("x"), None)), elem("items"))
    }
    "for with element + index binding" in {
      p("for x, i <- .items") shouldBe
        ForAST(Some((Ident("x"), Some(Ident("i")))), elem("items"))
    }
    "with" in {
      p("with .a") shouldBe WithAST(elem("a"))
    }
    "match" in {
      p("match x") shouldBe MatchAST(varExpr("x"))
    }
    "case" in {
      p("case 1") shouldBe CaseAST(num(1))
    }
    "assignment" in {
      p("v := 5") shouldBe AssignmentAST("v", num(5))
    }
    "define" in {
      p("define foo") shouldBe DefineAST(Ident("foo"))
    }
    "block" in {
      p("block foo .x") shouldBe BlockAST(Ident("foo"), elem("x"))
    }
    "return (no value)" in {
      p("return") shouldBe ReturnAST(None)
    }
    "return (with value)" in {
      p("return 5") shouldBe ReturnAST(Some(num(5)))
    }
    "comment" in {
      p("// some comment") shouldBe CommentAST("some comment")
    }
  }
}
