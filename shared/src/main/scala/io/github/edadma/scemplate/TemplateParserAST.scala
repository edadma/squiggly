package io.github.edadma.scemplate

trait TemplateParserAST

case class ContentAST(toks: Seq[Token]) extends TemplateParserAST

case class IfBlockAST(cond: ExprAST,
                      yes: TemplateParserAST,
                      elseif: Seq[(ExprAST, TemplateParserAST)],
                      no: Option[TemplateParserAST])
    extends TemplateParserAST

case class BlockAST(block: ConstructAST, body: TemplateParserAST) extends TemplateParserAST

case class SequenceAST(seq: List[TemplateParserAST]) extends TemplateParserAST
