package io.github.edadma.scemplate

import io.github.edadma.char_reader.CharReader

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer

class TemplateParser(input: String, startDelim: String, endDelim: String) {

  def tokenize(r: CharReader): (CharReader, Token) = {
    val buf = new ListBuffer[Char]

    if (r.more) {

      matches(r, startDelim) match {
      case Some(tagrest) =>
        if (buf.nonEmpty) {
          seq ++= text(buf.toString)
          buf.clear()
        }

        matchTag(tagrest) match {
          case Some((rest, s)) =>
        }
    }

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
  }

  trait Token {
    val pos: CharReader
  }

  case class TagToken(pos: CharReader, tag: TagParserAST) extends Token

  case class TextToken(pos: CharReader, text: String) extends Token

  case class WhitespaceToken(pos: CharReader, s: String) extends Token
}
