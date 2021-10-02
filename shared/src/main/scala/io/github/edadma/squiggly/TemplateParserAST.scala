package io.github.edadma.squiggly

import io.github.edadma.char_reader.CharReader

trait TemplateParserAST

case class ContentAST(toks: Seq[Token]) extends TemplateParserAST

case class IfBlockAST(cond: ExprAST,
                      body: TemplateParserAST,
                      elseif: Seq[(ExprAST, TemplateParserAST)],
                      els: Option[TemplateParserAST])
    extends TemplateParserAST

case class BlockAST(pos: CharReader, block: ConstructAST, body: TemplateParserAST, els: Option[TemplateParserAST])
    extends TemplateParserAST

case class BlockWithElseAST(body: TemplateParserAST) extends TemplateParserAST

case class BlockWithElseIfAST(body: TemplateParserAST, cond: ExprAST) extends TemplateParserAST

case class SequenceAST(seq: List[TemplateParserAST]) extends TemplateParserAST

case object EmptyBlockAST extends TemplateParserAST
