package io.github.edadma.scemplate

import scala.util.{Failure, Success}

import org.parboiled2._

import scala.language.implicitConversions

class TagParser(val input: ParserInput) extends Parser {

  implicit def wspStr(s: String): Rule0 = rule(str(s) ~ zeroOrMore(' '))

  def tag: Rule1[ExprAST] = rule(ws ~ expression ~ EOI)

  def expression: Rule1[ExprAST] = rule(applicative ~ zeroOrMore("|" ~ (apply | variable) ~> PipeExpr))

  def applicative: Rule1[ExprAST] = rule(apply | additive)

  def apply: Rule1[ApplyExpr] = rule(ident ~ oneOrMore(additive) ~> ApplyExpr)

  def additive: Rule1[ExprAST] =
    rule {
      multiplicative ~ zeroOrMore(
        "+" ~ multiplicative ~> AddExpr
          | "-" ~ multiplicative ~> SubExpr
      )
    }

  def multiplicative: Rule1[ExprAST] =
    rule {
      primary ~ zeroOrMore(
        "*" ~ primary ~> MulExpr
          | "/" ~ primary ~> DivExpr)
    }

  def primary: Rule1[ExprAST] = rule {
    number |
      variable |
      string |
      "(" ~ expression ~ ")"
  }

  def number: Rule1[NumberExpr] = rule(decimal ~> NumberExpr)

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

  def variable: Rule1[VarExpr] = rule(capture(optional('$')) ~ ident ~> VarExpr)

  def string: Rule1[StringExpr] =
    rule((singleQuoteString | doubleQuoteString) ~> ((s: String) => StringExpr(unescape(s))))

  def backtickString: Rule1[String] = rule(capture('`' ~ zeroOrMore("\\`" | noneOf("`"))) ~ '`' ~ ws)

  def singleQuoteString: Rule1[String] = rule('\'' ~ capture(zeroOrMore("\\'" | noneOf("'\n"))) ~ '\'' ~ ws)

  def doubleQuoteString: Rule1[String] = rule('"' ~ capture(zeroOrMore("\\\"" | noneOf("\"\n"))) ~ '"' ~ ws)

  def ident: Rule1[Ident] =
    rule {
      push(cursor) ~ capture((CharPredicate.Alpha | '_') ~ zeroOrMore(CharPredicate.AlphaNum | '_')) ~> Ident ~ ws
    }

  def ws: Rule0 = rule(zeroOrMore(' '))

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
