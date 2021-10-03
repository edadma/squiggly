package io.github.edadma.squiggly

import io.github.edadma.char_reader.CharReader

trait ParserAST

case class ContentAST(toks: Seq[Token]) extends ParserAST

case class IfBlockAST(cond: ExprAST, body: ParserAST, elseif: Seq[(ExprAST, ParserAST)], els: Option[ParserAST])
    extends ParserAST

case class TemplateBlockAST(pos: CharReader, block: ConstructAST, body: ParserAST, els: Option[ParserAST])
    extends ParserAST

case class DefineBlockAST(name: Ident, body: ParserAST) extends ParserAST

case class BlockBlockAST(name: Ident, body: ParserAST, expr: ExprAST) extends ParserAST

case class BlockWithElseAST(body: ParserAST) extends ParserAST

case class BlockWithElseIfAST(body: ParserAST, cond: ExprAST) extends ParserAST

case class SequenceAST(seq: List[ParserAST]) extends ParserAST

case object EmptyBlockAST extends ParserAST
