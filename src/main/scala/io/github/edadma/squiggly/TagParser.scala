package io.github.edadma.squiggly

import io.github.edadma.char_reader.CharReader

import scala.util.parsing.combinator.{ImplicitConversions, PackratParsers}
import scala.util.parsing.combinator.lexical.StdLexical
import scala.util.parsing.combinator.syntactical.StandardTokenParsers
import scala.util.parsing.input.CharSequenceReader

object TagParser extends StandardTokenParsers with PackratParsers with ImplicitConversions:
  override val lexical = new TagLexer

  def parseTag(input: String, startpos: CharReader, startoffset: Int): Expr =
    phrase(expression)(new lexical.Scanner(new PackratReader(new CharSequenceReader(input)))) match {
      case Success(ast, _) => ast
      case e: NoSuccess    => sys.error(s"parse error: $e")
    }

  lexical.reserved ++= ("""
      |if
      |then
      |else
      |div
      |and
      |or
      |not
      |mod
      |define
      |block
      |return
      |elsif
      |end
      |match
      |case
      |with
      |for
      |true
      |false
      |null
      |""".trim.stripMargin split "\\s+")
  lexical.delimiters ++= ("+ - * / ^ % ( ) [ ] { } ` | . , < <= > >= != =" split ' ')

  type P[+T] = PackratParser[T]

  lazy val expression: P[Expr] = ternary

  lazy val conditional: P[ExprAST] = positioned(
    ("if" ~> condition <~ "then") ~ conditional ~ opt("else" ~> conditional) ^^ ConditionalAST.apply
      | disjunctive,
  )

  lazy val condition: P[ExprAST] = disjunctive

  lazy val disjunctive: P[ExprAST] = positioned(
    disjunctive ~ ("or" ~> conjunctive) ^^ OrExpr.apply
      | conjunctive,
  )

  lazy val conjunctive: P[ExprAST] = positioned(
    conjunctive ~ ("and" ~> complement) ^^ AndExpr.apply
      | complement,
  )

  lazy val complement: P[ExprAST] = positioned(
    "not" ~ relational ^^ PrefixExpr.apply
      | relational,
  )

  lazy val relational: P[ExprAST] = positioned(
    pipe ~ rep1("<" | ">" | "<=" | ">=" | "=" | "!=") ~ pipe ^^ Tuple2.apply) ^^ CompareExpr.apply
      | pipe,
  )

  lazy val additive: P[ExprAST] = positioned(
    additive ~ ("+" | "-") ~ multiplicative ^^ LeftInfixExpr.apply
      | multiplicative,
  )

  lazy val multiplicative: P[Expr] = positioned(
    multiplicative ~ ("*" | "/" | "mod") ~ prefix ^^ Binary.apply
      | prefix,
  )

  lazy val prefix: P[Expr] = positioned(
    ("not" | "-") ~ prefix ^^ Unary.apply
      | exponentiation,
  )

  lazy val exponentiation: P[Expr] = positioned(
    postfix ~ "^" ~ prefix ^^ Binary.apply
      | postfix,
  )

  lazy val postfix: P[Expr] = positioned(
    applicative <~ "%" ^^ (e => Unary("%", e))
      | applicative,
  )

  lazy val applicative: P[Expr] = positioned(
    ident ~ ("(" ~> repsep(expression, ",") <~ ")") ^^ Apply.apply
      | primary,
  )

  lazy val primary: P[Expr] = positioned(
    ident ^^ Name.apply
      | numericLit ^^ NumericLit.apply
      | stringLit ^^ StringLit.apply
      | "(" ~> expression <~ ")",
  )
