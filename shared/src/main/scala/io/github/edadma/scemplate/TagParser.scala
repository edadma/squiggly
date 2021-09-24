package io.github.edadma.scemplate

import scala.util.{Failure, Success}

import org.parboiled2._

import scala.language.implicitConversions

class TagParser(val input: ParserInput, line: Int, col: Int) extends Parser {

  implicit def wsStr(s: String): Rule0 = rule(str(s) ~ ws)

  def tag: Rule1[TagParserAST] =
    rule {
      ws ~ (
        ifTag
          | elseTag
          | endTag
          | withTag
          | rangeTag
          | assignmentTag
          | expression
      ) ~ EOI
    }

  def expression: Rule1[ExprAST] = conditional

  def conditional: Rule1[ExprAST] =
    rule {
      ("if" ~ condition ~ "then" ~ expression ~ "else" ~ expression ~> ConditionalAST) | condition
    }

  def condition: Rule1[ExprAST] = disjunctive

  def disjunctive: Rule1[ExprAST] =
    rule {
      conjunctive ~ zeroOrMore(
        capture("or") ~ conjunctive ~> BinaryExpr
      )
    }

  def conjunctive: Rule1[ExprAST] =
    rule {
      not ~ zeroOrMore(
        capture("and") ~ not ~> BinaryExpr
      )
    }

  def not: Rule1[ExprAST] =
    rule {
      capture("not") ~ not ~> UnaryExpr |
        comparitive
    }

  def comparitive: Rule1[ExprAST] =
    rule {
      pipeline ~ oneOrMore(
        capture("<=" | ">=" | "!=" | "<" | ">" | "=") ~ pipeline ~> ((o: String, p: ExprAST) => (o, p))
      ) ~> CompareExpr |
        pipeline
    }

  def pipeline: Rule1[ExprAST] =
    rule {
      applicative ~ zeroOrMore(
        capture("|") ~ (apply | variable) ~> BinaryExpr
      )
    }

  def applicative: Rule1[ExprAST] = rule(apply | additive)

  def apply: Rule1[ApplyExpr] = rule(ident ~ oneOrMore(additive) ~> ApplyExpr)

  def additive: Rule1[ExprAST] =
    rule {
      multiplicative ~ zeroOrMore(
        capture("+") ~ multiplicative ~> BinaryExpr
          | capture("-") ~ multiplicative ~> BinaryExpr
      )
    }

  def multiplicative: Rule1[ExprAST] =
    rule {
      negative ~ zeroOrMore(
        capture("*") ~ negative ~> BinaryExpr
          | capture("/") ~ negative ~> BinaryExpr)
    }

  def negative: Rule1[ExprAST] =
    rule {
      capture("-") ~ negative ~> UnaryExpr |
        primary
    }

  def primary: Rule1[ExprAST] = rule {
    number |
      variable |
      string |
      element |
      "(" ~ expression ~ ")"
  }

  def number: Rule1[NumberExpr] = rule(pos ~ decimal ~> NumberExpr)

  def decimal: Rule1[BigDecimal] =
    rule {
      capture(
        (zeroOrMore(CharPredicate.Digit) ~ '.' ~ digits | digits ~ '.') ~
          optional((ch('e') | 'E') ~ optional(ch('+') | '-') ~ digits) |
          digits
      ) ~ ws ~> ((s: String) => BigDecimal(s))
    }

  def integer: Rule1[Int] = rule(capture(digits) ~ ws ~> ((s: String) => s.toInt))

  def digits: Rule0 = rule(oneOrMore(CharPredicate.Digit))

  def variable: Rule1[VarExpr] = rule(pos ~ capture(optional('$')) ~ ident ~> VarExpr)

  def element: Rule1[ElementExpr] = rule(pos ~ "." ~ zeroOrMore(ident).separatedBy(".") ~> ElementExpr)

  def string: Rule1[StringExpr] =
    rule(pos ~ (singleQuoteString | doubleQuoteString) ~> ((p: Int, s: String) => StringExpr(p, unescape(s))))

  def backtickString: Rule1[String] = rule(capture('`' ~ zeroOrMore("\\`" | noneOf("`"))) ~ '`' ~ ws)

  def singleQuoteString: Rule1[String] = rule('\'' ~ capture(zeroOrMore("\\'" | noneOf("'\n"))) ~ '\'' ~ ws)

  def doubleQuoteString: Rule1[String] = rule('"' ~ capture(zeroOrMore("\\\"" | noneOf("\"\n"))) ~ '"' ~ ws)

  def ident: Rule1[Ident] =
    rule {
      pos ~ capture((CharPredicate.Alpha | '_') ~ zeroOrMore(CharPredicate.AlphaNum | '_')) ~> Ident ~ ws
    }

  def pos: Rule1[Int] = rule(push(cursor))

  def ws: Rule0 = rule(quiet(zeroOrMore(anyOf(" \t\r\n"))))

  def assignmentTag: Rule1[AssignmentAST] = rule(capture(optional('$')) ~ ident ~ ":=" ~ expression ~> AssignmentAST)

  def ifTag: Rule1[IfAST] = rule(pos ~ "if" ~ condition ~> IfAST)

  def elseTag: Rule1[ElseAST] = rule(pos ~ "else" ~> ElseAST)

  def endTag: Rule1[EndAST] = rule(pos ~ "end" ~> EndAST)

  def withTag: Rule1[IfAST] = rule(pos ~ "with" ~ expression ~> IfAST)

  def rangeTag: Rule1[IfAST] = rule(pos ~ "range" ~ expression ~> IfAST)

  def parseTag: TagParserAST =
    tag.run() match {
      case Success(ast)           => ast
      case Failure(e: ParseError) => sys.error("Expression is not valid: " + formatError(e))
      case Failure(e)             => sys.error("Unexpected error during parsing run: " + e)
    }

  def parseExpression: ExprAST =
    expression.run() match {
      case Success(ast)           => ast
      case Failure(e: ParseError) => sys.error("Expression is not valid: " + formatError(e))
      case Failure(e)             => sys.error("Unexpected error during parsing run: " + e)
    }

}
