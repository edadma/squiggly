package io.github.edadma.scemplate

trait TemplateParserAST

case class BodyAST(toks: Seq[Token]) extends TemplateParserAST

case class IfBlockAST(cond: ExprAST, yes: BodyAST, elseif: Seq[(ExprAST, BodyAST)], no: Option[BodyAST])
    extends TemplateParserAST

case class BlockAST(block: ConstructAST, body: BodyAST) extends TemplateParserAST
