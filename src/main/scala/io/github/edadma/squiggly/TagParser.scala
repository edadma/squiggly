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
  lexical.delimiters ++= ("+ - * / ^ % ( ) [ ] { } ` | . , < <= > >= == != =" split ' ')

  type P[+T] = PackratParser[T]

  lazy val formulae: P[Seq[Decl]] = rep1(declaration)

  lazy val declaration: P[Decl] = positioned(
    "def" ~> ident ~ ("=" ~> expression) ^^ Formula.apply
      | "const" ~> ident ~ "=" ~ expression ^^ {
        case n ~ _ ~ NumericLit(v) => Val(n, v.toDouble)
        case n ~ _ ~ StringLit(v)  => Val(n, v)
        case n ~ _ ~ e             => Const(n, e, null)
      }
      | "def" ~> ident ~ ("(" ~> rep1sep(ident, ",") <~ ")") ~ ("=" ~> expression) ^^ Def.apply
      | "var" ~> ident ~ opt("=" ~> expression) ^^ { case n ~ e => Var(n, e.orNull, null) },
  )

  lazy val expression: P[Expr] = ternary

  lazy val ternary: P[Expr] = positioned(
    disjunctive ~ ("?" ~> disjunctive) ~ (":" ~> ternary) ^^ Ternary.apply
      | disjunctive,
  )

  lazy val disjunctive: P[Expr] = positioned(
    disjunctive ~ "or" ~ conjunctive ^^ Binary.apply
      | conjunctive,
  )

  lazy val conjunctive: P[Expr] = positioned(
    conjunctive ~ "and" ~ relational ^^ Binary.apply
      | relational,
  )

  lazy val relational: P[Expr] = positioned(
    additive ~ ("<" | ">" | "<=" | ">=" | "==" | "!=") ~ additive ^^ Binary.apply
      | additive,
  )

  lazy val additive: P[Expr] = positioned(
    additive ~ ("+" | "-") ~ multiplicative ^^ Binary.apply
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
