package io.github.edadma.squiggly

import io.github.edadma.char_reader.CharReader

trait Token {
  val pos: CharReader

  def eoi: Boolean = isInstanceOf[EOIToken]
}

case class EOIToken(pos: CharReader) extends Token

case class TagToken(pos: CharReader, tag: TagParserAST, trimLeft: Boolean, trimRight: Boolean) extends Token

case class TextToken(pos: CharReader, text: String) extends Token

case class SpaceToken(pos: CharReader, s: String) extends Token
