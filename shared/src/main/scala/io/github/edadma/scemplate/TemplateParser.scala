package io.github.edadma.scemplate

import io.github.edadma.char_reader.CharReader

import scala.annotation.tailrec

class TemplateParser(input: String, startDelim: String, endDelim: String) {

  def tokenize: LazyList[Token] = tokenize(CharReader.fromString(input))

  def tokenize(r: CharReader): LazyList[Token] =
    token(r, r) match {
      case Some((rest, tok)) => tok #:: tokenize(rest)
      case None              => LazyList.empty
    }

  def token(r: CharReader, start: CharReader, buf: StringBuilder = new StringBuilder): Option[(CharReader, Token)] = {
    @tailrec
    def matchTag(r: CharReader, buf: StringBuilder = new StringBuilder): Option[(CharReader, String)] = {
      if (r.eoi) None
      else {
        matches(r, endDelim) match {
          case Some(rest) => Some((rest, buf.toString))
          case None =>
            buf += r.ch
            matchTag(r.next, buf)
        }
      }
    }

    def matches(r: CharReader, s: String): Option[CharReader] = {
      @tailrec
      def matches(r: CharReader, s: List[Char]): Option[CharReader] =
        s match {
          case head :: tail =>
            if (head == r.ch) matches(r.next, tail)
            else None
          case Nil => Some(r)
        }

      matches(r, s.toList)
    }

    if (r.more) {
      matches(r, startDelim) match {
        case Some(tagrest) =>
          matchTag(tagrest) match {
            case Some((rest, tag)) =>
              val tagParser = new TagParser(tag)
              val ast = tagParser.parseTag

              Some((rest, TagToken(r, ast)))
            case None => r.error("unclosed tag")
          }
        case None =>
          if (buf.nonEmpty && (r.ch.isWhitespace ^ buf.last.isWhitespace))
            Some((r, if (buf.last.isWhitespace) SpaceToken(start, buf.toString) else TextToken(start, buf.toString)))
          else {
            buf += r.ch
            token(r.next, start, buf)
          }
      }
    } else if (buf.isEmpty) None
    else Some((r, if (buf.last.isWhitespace) SpaceToken(start, buf.toString) else TextToken(start, buf.toString)))
  }

  trait Token {
    val pos: CharReader
  }

  case class TagToken(pos: CharReader, tag: TagParserAST) extends Token

  case class TextToken(pos: CharReader, text: String) extends Token

  case class SpaceToken(pos: CharReader, s: String) extends Token
}
