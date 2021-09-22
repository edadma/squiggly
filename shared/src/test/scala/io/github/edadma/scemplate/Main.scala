package io.github.edadma.scemplate

import scala.util.{Failure, Success}
import org.parboiled2._
import pprint._

import scala.language.implicitConversions

object Main extends App {

  val tag = " a + 2 * b "

  val parser = new TagParser(tag)

  parser.tag.run() match {
    case Success(ast)           => pprintln(ast)
    case Failure(e: ParseError) => println("Expression is not valid: " + parser.formatError(e))
    case Failure(e)             => println("Unexpected error during parsing run: " + e)
  }

}

class TagParser(val input: ParserInput) extends Parser {

  implicit def wspStr(s: String): Rule0 = rule(str(s) ~ zeroOrMore(' '))

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

  def ident: Rule1[String] = rule(capture((CharPredicate.Alpha | '_') ~ zeroOrMore(CharPredicate.AlphaNum | '_')) ~ ws)

  def ws: Rule0 = rule(zeroOrMore(' '))

}

trait AST

trait ExprAST

case class ValueExpr(value: String) extends ExprAST

case class VarExpr(ident: String) extends ExprAST

case class AddExpr(left: ExprAST, right: ExprAST) extends ExprAST

case class SubExpr(left: ExprAST, right: ExprAST) extends ExprAST

case class MulExpr(left: ExprAST, right: ExprAST) extends ExprAST

case class DivExpr(left: ExprAST, right: ExprAST) extends ExprAST
