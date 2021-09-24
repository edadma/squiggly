package io.github.edadma.scemplate

import io.github.edadma.char_reader.CharReader

trait Token {
  val pos: CharReader
}

case class TagToken(pos: CharReader, tag: TagParserAST, trimLeft: Boolean, trimRight: Boolean) extends Token

case class TextToken(pos: CharReader, text: String) extends Token

case class SpaceToken(pos: CharReader, s: String) extends Token
