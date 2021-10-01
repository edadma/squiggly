package io.github.edadma.scemplate

import io.github.edadma.char_reader.CharReader

import scala.annotation.tailrec
import scala.collection.mutable.{ArrayBuffer, ListBuffer}

class TemplateParser(input: String,
                     startDelim: String,
                     endDelim: String,
                     functions: Map[String, BuiltinFunction],
                     namespaces: Map[String, Map[String, BuiltinFunction]]) {

  def parse: TemplateParserAST = {
    val seq = new ListBuffer[TemplateParserAST]

    def parse(ts: LazyList[Token],
              parsingbody: Boolean,
              allowelse: Boolean,
              tokbuf: ListBuffer[Token] = new ListBuffer,
              astbuf: ListBuffer[TemplateParserAST] = new ListBuffer): (TemplateParserAST, LazyList[Token]) = {
      def endOfBlock: TemplateParserAST = {
        if (tokbuf.nonEmpty)
          astbuf += ContentAST(tokbuf.toList)

        if (astbuf.isEmpty) EmptyBlockAST
        else if (astbuf.length == 1) astbuf.head
        else SequenceAST(astbuf.toList)
      }

      ts match {
        case (h @ TagToken(_, _, _, true)) #:: (_: SpaceToken) #:: t =>
          parse(h #:: t, parsingbody, allowelse, tokbuf, astbuf)
        case (_: SpaceToken) #:: (h @ TagToken(_, _, true, _)) #:: t =>
          parse(h #:: t, parsingbody, allowelse, tokbuf, astbuf)
        case TagToken(pos, tag: SimpleBlockAST, _, _) #:: t =>
          if (tokbuf.nonEmpty) {
            astbuf += ContentAST(tokbuf.toList)
            tokbuf.clear()
          }

          val (body0, rest0) = parse(t, parsingbody = true, allowelse = true)
          val (body, els, rest) =
            body0 match {
              case BlockWithElseAST(b) =>
                val (els, rest) = parse(rest0, parsingbody = true, allowelse = false)

                (b, Some(els), rest)
              case _ => (body0, None, rest0)
            }

          if (body == EmptyBlockAST)
            Console.err.println(pos.longErrorText("warning: empty block"))

          astbuf += BlockAST(pos, tag, body, els)
          parse(rest, parsingbody, allowelse, tokbuf, astbuf)
        case TagToken(pos, _: ElseAST, _, _) #:: t =>
          if (parsingbody && allowelse) (BlockWithElseAST(endOfBlock), t)
          else pos.error("unexpected 'else' tag")
        case TagToken(pos, ElseIfAST(_, cond), _, _) #:: t =>
          if (parsingbody && allowelse) (BlockWithElseIfAST(endOfBlock, cond), t)
          else pos.error("unexpected 'else if' tag")
        case TagToken(pos, _: EndAST, _, _) #:: t =>
          if (parsingbody) (endOfBlock, t)
          else pos.error("unexpected 'end' tag")
        case TagToken(pos, IfAST(_, cond), _, _) #:: t =>
          if (tokbuf.nonEmpty) {
            astbuf += ContentAST(tokbuf.toList)
            tokbuf.clear()
          }

          val elseifbuf = new ArrayBuffer[(ExprAST, TemplateParserAST)]

          @tailrec
          def elseif(t: LazyList[Token]): (TemplateParserAST, Option[TemplateParserAST], LazyList[Token]) = {
            val (body0, rest0) = parse(t, parsingbody = true, allowelse = true)

            body0 match {
              case BlockWithElseAST(b) =>
                val (els, rest) = parse(rest0, parsingbody = true, allowelse = false)

                (b, Some(els), rest)
              case BlockWithElseIfAST(b, cond) =>
                elseifbuf += ((cond, b))
                elseif(rest0)
              case _ => (body0, None, rest0)
            }
          }

          val (body, els, rest) = elseif(t)

          if (body == EmptyBlockAST)
            Console.err.println(pos.longErrorText("warning: empty block"))

          var next: TemplateParserAST = body

          for (i <- elseifbuf.indices.reverse) {
            val cur = elseifbuf(i)._2

            elseifbuf(i) = (elseifbuf(i)._1, next)
            next = cur
          }

          astbuf += IfBlockAST(cond, next, elseifbuf.toSeq, els)
          parse(rest, parsingbody, allowelse, tokbuf, astbuf)
        case h #:: t if !h.eoi =>
          tokbuf += h
          parse(t, parsingbody, allowelse, tokbuf, astbuf)
        case EOIToken(pos) #:: _ =>
          if (parsingbody) pos.error("missing end tag")
          else (endOfBlock, LazyList.empty)
      }
    }

    parse(tokenize, parsingbody = false, allowelse = false)._1
  }

  def tokenize: LazyList[Token] = tokenize(CharReader.fromString(input))

  def tokenize(r: CharReader): LazyList[Token] =
    token(r, r) match {
      case Some((rest, tok)) => tok #:: tokenize(rest)
      case None              => EOIToken(r) #:: LazyList.empty
    }

  def token(r: CharReader, start: CharReader, buf: StringBuilder = new StringBuilder): Option[(CharReader, Token)] = {
    def text(r: CharReader): (CharReader, Token) =
      (r, if (buf.last.isWhitespace) SpaceToken(start, buf.toString) else TextToken(start, buf.toString))

    if (r.more) {
      r.matchDelimited(startDelim, endDelim) match {
        case None => r.error("unclosed tag")
        case Some(Some((tag, rest))) =>
          if (buf.isEmpty) {
            val trimLeft = tag.length >= 2 && tag.startsWith("-") && tag(1).isWhitespace
            val tag1 = if (trimLeft) tag drop 2 else tag
            val trimRight = tag1.length >= 2 && tag1.endsWith("-") && tag1(tag1.length - 2).isWhitespace
            val tag2 = if (trimRight) tag1 dropRight 2 else tag1
            val tagParser = new TagParser(tag2, rest.line, rest.col, functions, namespaces)
            val ast = tagParser.parseTag

            Some((rest, TagToken(r, ast, trimLeft, trimRight)))
          } else Some(text(r))
        case Some(None) =>
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
