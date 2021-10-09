package io.github.edadma.squiggly

case class TagParserIdent(pos: TagParser#Position, name: String)

trait TagParserAST

trait Positioned {
  val pos: TagParser#Position
}

trait ExprAST extends TagParserAST

trait SimpleExpr extends ExprAST with Positioned

case class StringExpr(pos: TagParser#Position, s: String) extends SimpleExpr

case class NumberExpr(pos: TagParser#Position, n: BigDecimal) extends SimpleExpr

case class BooleanExpr(pos: TagParser#Position, b: Boolean) extends SimpleExpr

case class NullExpr(pos: TagParser#Position) extends SimpleExpr

case class VarExpr(pos: TagParser#Position, user: String, name: TagParserIdent) extends SimpleExpr

case class ElementExpr(pos: TagParser#Position, global: String, ids: Seq[TagParserIdent]) extends SimpleExpr

case class MapExpr(pairs: Seq[(TagParserIdent, TagParser#Position, ExprAST)]) extends ExprAST

case class SeqExpr(elems: Seq[ExprAST]) extends ExprAST

case class PrefixExpr(op: String, pos: TagParser#Position, expr: ExprAST) extends ExprAST

case class LeftInfixExpr(lpos: TagParser#Position, left: ExprAST, right: Seq[(String, TagParser#Position, ExprAST)])
    extends ExprAST

case class RightInfixExpr(lpos: TagParser#Position, left: ExprAST, op: String, rpos: TagParser#Position, right: ExprAST)
    extends ExprAST

case class ApplyExpr(name: TagParserIdent, args: Seq[ExprAST]) extends ExprAST

case class ConditionalAST(cond: ExprAST, yes: ExprAST, no: Option[ExprAST]) extends ExprAST

case class OrExpr(left: ExprAST, right: ExprAST) extends ExprAST

case class AndExpr(left: ExprAST, right: ExprAST) extends ExprAST

case class CompareExpr(lpos: TagParser#Position, left: ExprAST, right: Seq[(String, TagParser#Position, ExprAST)])
    extends ExprAST

case class MethodExpr(expr: ExprAST, method: TagParserIdent) extends ExprAST

case class IndexExpr(expr: ExprAST, pos: TagParser#Position, index: ExprAST) extends ExprAST

case class PipeExpr(left: ExprAST, right: ApplyExpr) extends ExprAST

case class NonStrictExpr(expr: ExprAST) extends ExprAST

case class AssignmentAST(name: TagParserIdent, expr: ExprAST) extends TagParserAST

case class ReturnAST(expr: Option[ExprAST]) extends TagParserAST

case class CommentAST(comment: String) extends TagParserAST

trait ConstructAST extends TagParserAST with Positioned

trait SimpleBlockAST extends ConstructAST

trait BasicBlockAST extends ConstructAST

case class IfAST(pos: TagParser#Position, cond: ExprAST) extends ConstructAST

case class ElseIfAST(pos: TagParser#Position, cond: ExprAST) extends ConstructAST

case class ElseAST(pos: TagParser#Position) extends ConstructAST

case class EndAST(pos: TagParser#Position) extends ConstructAST

case class MatchAST(pos: TagParser#Position, cond: ExprAST) extends ConstructAST

case class CaseAST(pos: TagParser#Position, cond: ExprAST) extends ConstructAST

case class WithAST(pos: TagParser#Position, expr: ExprAST) extends SimpleBlockAST

case class ForAST(index: Option[(TagParserIdent, Option[TagParserIdent])], pos: TagParser#Position, expr: ExprAST)
    extends SimpleBlockAST

case class DefineAST(pos: TagParser#Position, name: TagParserIdent) extends BasicBlockAST

case class BlockAST(pos: TagParser#Position, name: TagParserIdent, expr: ExprAST) extends BasicBlockAST
