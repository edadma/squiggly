package io.github.edadma

//import io.github.edadma.commonmark.CommonMarkParser

import scala.collection.{immutable, mutable}
import scala.util.parsing.input.Positional

package object squiggly {

  type Blocks = mutable.HashMap[String, TemplateAST]

  case class TemplateFunction(name: String, arity: Int, function: PartialFunction[(Context, Seq[Any]), Any])

  type TemplateLoader = String => Option[TemplateAST]

  def restrict(pos: Positional, v: Any): Any =
    v match {
      case () => error(pos, "attempting to bind a value of 'undefined'")
      case _  => v
    }

  type Num = BigDecimal

  trait Platform {
    def yaml(s: String): Any
  }

  // lazy val platform: Platform = squiggly.platformSpecific

  private[squiggly] val ZERO = BigDecimal(0)

  def falsy(a: Any): Boolean =
    a match {
      case () | false | null | "" | ZERO          => true
      case s: collection.Iterable[_] if s.isEmpty => true
      case _                                      => false
    }

  private val HEX = {
    val a = new Array[Int](128)

    List(
      '0' -> 0,
      '1' -> 1,
      '2' -> 2,
      '3' -> 3,
      '4' -> 4,
      '5' -> 5,
      '6' -> 6,
      '7' -> 7,
      '8' -> 8,
      '9' -> 9,
      'a' -> 10,
      'A' -> 10,
      'b' -> 11,
      'B' -> 11,
      'c' -> 12,
      'C' -> 12,
      'd' -> 13,
      'D' -> 13,
      'e' -> 14,
      'E' -> 14,
      'f' -> 15,
      'F' -> 15,
    ) foreach { case (k, v) => a(k) = v }

    a to immutable.ArraySeq
  }

  def hex(c: Char): Int = if (c < 128) HEX(c) else 0

  def unescape(pos: TagParser#Position, s: String): String = {
    val buf = new StringBuilder
    val it = s.iterator
    var idx = -1

    def ch =
      if (it.hasNext) {
        idx += 1
        it.next()
      } else pos.shift(idx).error("unexpected end of string")

    while (it.hasNext) {
      ch match {
        case '\\' =>
          buf +=
            (ch match {
              case '\\' => '\\'
              case '"'  => '"'
              case '\'' => '\''
              case '/'  => '/'
              case 'b'  => '\b'
              case 'f'  => '\f'
              case 'n'  => '\n'
              case 'r'  => '\r'
              case 't'  => '\t'
              case 'u'  => (hex(ch) << 12 | hex(ch) << 8 | hex(ch) << 4 | hex(ch)).toChar
              case c    => pos.shift(idx + 1).error(s"non-escapable character: '$c' (${c.toInt})")
            })
        case c => buf += c
      }
    }

    buf.toString
  }

  //  def problem(pos: Int, parser: Parser, msg: String, line: Int, col: Int): Nothing = {
  //    val p = Position(pos, parser.input)
  //    //    val offset = p.copy(line = p.line + line - 1, column = p.column + col - 1)
  //
  //    sys.error(new RuntimeErrorFormatter(msg).customFormat(ParseError(p, p, Nil), parser.input))
  //  }

  class ReturnException extends RuntimeException

//  lazy val markdownParser = new CommonMarkParser

  case class BaseURL(base: String, path: String)

  //  def render(v: Any): String = {
  //    def render =
  //      v match {
  //        case s: collection.Seq[_] => s.mkString("[", ", ", "]")
  //        case m: collection.Map[_, _] => m map { case (k, v) => s"$k: ${render(v)}" } mkString("[", ", ", "]")
  //        case s: String => s""""$s""""
  //        case null | () => ""
  //        case v => v.toString
  //      }
  //
  //    (context.eval(tag) match {
  //      case s: String => s
  //      case v => render(v)
  //    }
  //
  //  }

}
