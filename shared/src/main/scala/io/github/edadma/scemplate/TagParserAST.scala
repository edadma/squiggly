package io.github.edadma.scemplate

case class Ident(pos: Int, name: String)

trait TagParserAST

trait ExprAST extends TagParserAST

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
