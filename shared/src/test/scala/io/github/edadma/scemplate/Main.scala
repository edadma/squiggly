package io.github.edadma.scemplate

import scala.util.{Failure, Success}
import org.parboiled2._
import pprint._

import scala.language.{implicitConversions, postfixOps}

object Main extends App {

  val tag = " abc "

  val parser = new TagParser(tag)

  parser.tag.run() match {
    case Success(ast) =>
      pprintln(ast)

      val p = ast.asInstanceOf[VarExpr].ident.pos

      problem(p, parser, "asdf")
    case Failure(e: ParseError) => println("Expression is not valid: " + parser.formatError(e))
    case Failure(e)             => println("Unexpected error during parsing run: " + e)
  }

  def problem(pos: Position, parser: Parser, msg: String): Nothing = {
    Console.err.println(new RuntimeErrorFormatter(msg).customFormat(ParseError(pos, pos, Nil), parser.input))
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

  def pos: Position = Position(cursor, input)

  def tag: Rule1[ExprAST] = rule(ws ~ expression ~ EOI)

  def expression: Rule1[ExprAST] =
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

  def primary: Rule1[ExprAST] = rule(number | variable)

  def number: Rule1[ValueExpr] = rule(capture(digits) ~> ValueExpr ~ ws)

  def digits: Rule0 = rule(oneOrMore(CharPredicate.Digit))

  def variable: Rule1[VarExpr] = rule(ident ~> VarExpr)

  def ident: Rule1[Ident] =
    rule {
      push(pos) ~ capture((CharPredicate.Alpha | '_') ~ zeroOrMore(CharPredicate.AlphaNum | '_')) ~> Ident ~ ws
    }

  def ws: Rule0 = rule(zeroOrMore(' '))

}

case class Ident(pos: Position, name: String)

trait AST

trait ExprAST

case class ValueExpr(value: String) extends ExprAST

case class VarExpr(ident: Ident) extends ExprAST

case class AddExpr(left: ExprAST, right: ExprAST) extends ExprAST

case class SubExpr(left: ExprAST, right: ExprAST) extends ExprAST

case class MulExpr(left: ExprAST, right: ExprAST) extends ExprAST

case class DivExpr(left: ExprAST, right: ExprAST) extends ExprAST
