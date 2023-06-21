package io.github.edadma.squiggly

import scala.util.parsing.input.Positional

trait TagParserAST extends Positional

trait ExprAST extends TagParserAST

case class Ident(name: String) extends TagParserAST

case class StringExpr(s: String) extends ExprAST

case class NumberExpr(n: BigDecimal) extends ExprAST

case class BooleanExpr(b: Boolean) extends ExprAST

case class NullExpr() extends ExprAST

case class VarExpr(user: String, name: Ident) extends ExprAST

case class ElementExpr(global: String, ids: Seq[Ident]) extends ExprAST

case class MapExpr(pairs: Seq[(Ident, ExprAST)]) extends ExprAST

case class SeqExpr(elems: Seq[ExprAST]) extends ExprAST

case class PrefixExpr(op: String, expr: ExprAST) extends ExprAST

case class LeftInfixExpr(left: ExprAST, op: String, right: ExprAST) extends ExprAST

case class RightInfixExpr(left: ExprAST, op: String, right: ExprAST) extends ExprAST

case class ApplyExpr(id: Ident, args: Seq[ExprAST]) extends ExprAST

case class ConditionalAST(cond: ExprAST, yes: ExprAST, no: Option[ExprAST]) extends ExprAST

case class OrExpr(left: ExprAST, right: ExprAST) extends ExprAST

case class AndExpr(left: ExprAST, right: ExprAST) extends ExprAST

case class CompareExpr(left: ExprAST, right: Seq[(String, ExprAST)]) extends ExprAST

case class MethodExpr(expr: ExprAST, method: Ident) extends ExprAST

case class IndexExpr(expr: ExprAST, index: ExprAST) extends ExprAST

case class PipeExpr(left: ExprAST, right: ApplyExpr) extends ExprAST

case class NonStrictExpr(expr: ExprAST) extends ExprAST

case class AssignmentAST(name: String, expr: ExprAST) extends TagParserAST

case class ReturnAST(expr: Option[ExprAST]) extends TagParserAST

case class CommentAST(comment: String) extends TagParserAST

trait ConstructAST extends TagParserAST

trait SimpleBlockAST extends ConstructAST

trait BasicBlockAST extends ConstructAST

case class IfAST(cond: ExprAST) extends ConstructAST

case class ElseIfAST(cond: ExprAST) extends ConstructAST

case class ElseAST() extends ConstructAST

case class EndAST() extends ConstructAST

case class MatchAST(cond: ExprAST) extends ConstructAST

case class CaseAST(cond: ExprAST) extends ConstructAST

case class WithAST(expr: ExprAST) extends SimpleBlockAST

case class ForAST(index: Option[(Ident, Option[Ident])], expr: ExprAST) extends SimpleBlockAST

case class DefineAST(name: Ident) extends BasicBlockAST

case class BlockAST(name: Ident, expr: ExprAST) extends BasicBlockAST
