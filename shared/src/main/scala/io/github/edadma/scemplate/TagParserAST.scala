package io.github.edadma.scemplate

case class Ident(pos: Int, name: String)

trait TagParserAST

trait ExprAST extends TagParserAST

case class StringExpr(s: String) extends ExprAST

case class NumberExpr(n: BigDecimal) extends ExprAST

case class VarExpr(user: String, name: Ident) extends ExprAST

case class NegExpr(expr: ExprAST) extends ExprAST

case class NotExpr(expr: ExprAST) extends ExprAST

case class AddExpr(left: ExprAST, right: ExprAST) extends ExprAST

case class SubExpr(left: ExprAST, right: ExprAST) extends ExprAST

case class MulExpr(left: ExprAST, right: ExprAST) extends ExprAST

case class DivExpr(left: ExprAST, right: ExprAST) extends ExprAST

case class AndExpr(left: ExprAST, right: ExprAST) extends ExprAST

case class OrExpr(left: ExprAST, right: ExprAST) extends ExprAST

case class ApplyExpr(name: Ident, args: Seq[ExprAST]) extends ExprAST

case class PipeExpr(left: ExprAST, right: ExprAST) extends ExprAST

case class ConcatExpr(elems: Seq[ExprAST]) extends ExprAST

case class ConditionalAST(cond: ExprAST, yes: ExprAST, no: ExprAST) extends ExprAST

case class AssignmentAST(user: String, name: Ident, right: ExprAST) extends TagParserAST

trait ConstructAST extends TagParserAST { val pos: Int }

case class IfAST(pos: Int, cond: ExprAST) extends ConstructAST

case class ElseAST(pos: Int) extends ConstructAST

case class EndAST(pos: Int) extends ConstructAST

case class WithAST(pos: Int, expr: ExprAST) extends ConstructAST

case class RangeAST(pos: Int, expr: ExprAST) extends ConstructAST
