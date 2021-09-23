package io.github.edadma.scemplate

import scala.util.{Failure, Success}
import org.parboiled2._
import pprint._

import scala.language.{implicitConversions, postfixOps}

object Main extends App {

  val tag = " (f $a + 3 b) + 4 | g 'asdf' y "

  val parser = new TagParser(tag)

  parser.tag.run() match {
    case Success(ast)           => pprintln(ast)
    case Failure(e: ParseError) => println("Expression is not valid: " + parser.formatError(e))
    case Failure(e)             => println("Unexpected error during parsing run: " + e)
  }

  def problem(pos: Int, parser: Parser, msg: String): Nothing = {
    val p = Position(pos, parser.input)

    Console.err.println(new RuntimeErrorFormatter(msg).customFormat(ParseError(p, p, Nil), parser.input))
    sys.exit(1)
  }

}

class RuntimeErrorFormatter(msg: String) extends ErrorFormatter {

  import java.lang.{StringBuilder => JStringBuilder}

  def customFormat(error: ParseError, input: ParserInput): String = {
    import error._

    val sb = new JStringBuilder(128)

    sb.append(msg)
    sb.append(" (line ").append(position.line).append(", column ").append(position.column).append(')')
    formatErrorLine(sb.append(':').append('\n'), error, input).toString
  }

}

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

  def singleQuoteString: Rule1[String] = rule(capture('\'' ~ zeroOrMore("\\'" | noneOf("'\n"))) ~ '\'' ~ ws)

  def doubleQuoteString: Rule1[String] = rule(capture('"' ~ zeroOrMore("\\\"" | noneOf("\"\n"))) ~ '"' ~ ws)

  def ident: Rule1[Ident] =
    rule {
      push(cursor) ~ capture((CharPredicate.Alpha | '_') ~ zeroOrMore(CharPredicate.AlphaNum | '_')) ~> Ident ~ ws
    }

  def ws: Rule0 = rule(zeroOrMore(' '))

  def unescape(s: String): String = s

}

case class Ident(pos: Int, name: String)

trait AST

trait ExprAST

case class StringExpr(s: String) extends ExprAST

case class NumberExpr(n: BigDecimal) extends ExprAST

case class VarExpr(user: String, name: Ident) extends ExprAST

case class AddExpr(left: ExprAST, right: ExprAST) extends ExprAST

case class SubExpr(left: ExprAST, right: ExprAST) extends ExprAST

case class MulExpr(left: ExprAST, right: ExprAST) extends ExprAST

case class DivExpr(left: ExprAST, right: ExprAST) extends ExprAST

case class ApplyExpr(name: Ident, args: Seq[ExprAST]) extends ExprAST

case class PipeExpr(left: ExprAST, right: ExprAST) extends ExprAST

case class ConcatExpr(elems: Seq[ExprAST]) extends ExprAST
