package io.github.edadma.scemplate

import io.github.edadma.char_reader.CharReader

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer

class TemplateParser(input: String, startDelim: String, endDelim: String) {

  def parse: TemplateParserAST = {
    val seq = new ListBuffer[TemplateParserAST]

    def parse(ts: LazyList[Token],
              parsingbody: Boolean,
              tokbuf: ListBuffer[Token] = new ListBuffer,
              astbuf: ListBuffer[TemplateParserAST] = new ListBuffer): (TemplateParserAST, LazyList[Token]) = {
      def endOfBlock(tail: LazyList[Token]): (TemplateParserAST, LazyList[Token]) = {
        if (tokbuf.nonEmpty)
          astbuf += ContentAST(tokbuf.toList)

        if (astbuf.isEmpty) (EmptyAST, tail)
        else if (astbuf.length == 1) (astbuf.head, tail)
        else (SequenceAST(astbuf.toList), tail)
      }

      ts match {
        case (h @ TagToken(_, _, _, true)) #:: (_: SpaceToken) #:: t => parse(h #:: t, parsingbody, tokbuf, astbuf)
        case (_: SpaceToken) #:: (h @ TagToken(_, _, true, _)) #:: t => parse(h #:: t, parsingbody, tokbuf, astbuf)
        case TagToken(pos, tag: WithAST, _, _) #:: t =>
          if (tokbuf.nonEmpty) {
            astbuf += ContentAST(tokbuf.toList)
            tokbuf.clear()
          }

          val (body, rest) = parse(t, parsingbody = true)

          astbuf += BlockAST(tag, body)
          parse(rest, parsingbody, tokbuf, astbuf)
        case TagToken(pos, _: EndAST, _, _) #:: t =>
          if (parsingbody)
            endOfBlock(t)
          else pos.error("unexpected end tag")
        case h #:: t =>
          tokbuf += h
          parse(t, parsingbody, tokbuf, astbuf)
        case _ =>
          if (parsingbody) sys.error("missing end tag")
          else endOfBlock(LazyList.empty)
      }
    }

    parse(tokenize, parsingbody = false)._1
  }

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

    def text(r: CharReader): (CharReader, Token) =
      (r, if (buf.last.isWhitespace) SpaceToken(start, buf.toString) else TextToken(start, buf.toString))

    if (r.more) {
      matches(r, startDelim) match {
        case Some(tagrest) =>
          matchTag(tagrest) match {
            case Some((rest, tag)) =>
              if (buf.isEmpty) {
                val trimLeft = tag.startsWith("-")
                val tag1 = if (trimLeft) tag drop 1 else tag
                val trimRight = tag1.endsWith("-")
                val tag2 = if (trimRight) tag1 dropRight 1 else tag1
                val tagParser = new TagParser(tag2, tagrest.line, tagrest.col)
                val ast = tagParser.parseTag

                Some((rest, TagToken(r, ast, trimLeft, trimRight)))
              } else Some(text(r))
            case None => r.error("unclosed tag")
          }
        case None =>
          if (buf.nonEmpty && (r.ch.isWhitespace ^ buf.last.isWhitespace)) Some(text(r))
          else {
            buf += r.ch
            token(r.next, start, buf)
          }
      }
    } else if (buf.isEmpty) None
    else Some(text(r))
  }
}
