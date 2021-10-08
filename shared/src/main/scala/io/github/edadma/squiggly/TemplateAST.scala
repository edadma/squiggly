package io.github.edadma.squiggly

import io.github.edadma.char_reader.CharReader

trait TemplateAST

case class ContentAST(toks: Seq[Token]) extends TemplateAST

case class IfBlockAST(cond: ExprAST, body: TemplateAST, elseif: Seq[(ExprAST, TemplateAST)], els: Option[TemplateAST])
    extends TemplateAST

case class MatchBlockAST(expr: ExprAST, cases: Seq[(ExprAST, TemplateAST)], els: Option[TemplateAST])
    extends TemplateAST

case class TemplateBlockAST(pos: CharReader, block: ConstructAST, body: TemplateAST, els: Option[TemplateAST])
    extends TemplateAST

case class DefineBlockAST(name: Ident, body: TemplateAST) extends TemplateAST

case class BlockBlockAST(name: Ident, body: TemplateAST, expr: ExprAST) extends TemplateAST

case class BlockWithElseAST(body: TemplateAST) extends TemplateAST

case class BlockWithElseIfAST(body: TemplateAST, cond: ExprAST) extends TemplateAST

case class BlockWithCaseAST(body: TemplateAST, expr: ExprAST) extends TemplateAST

case class SequenceAST(seq: List[TemplateAST]) extends TemplateAST

case object EmptyBlockAST extends TemplateAST
