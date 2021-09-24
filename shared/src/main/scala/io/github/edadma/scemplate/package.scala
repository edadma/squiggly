package io.github.edadma

import org.parboiled2.{ParseError, Parser, Position}

import scala.collection.immutable.ArraySeq

package object scemplate {

  trait Platform {
    def yaml(s: String): Any
  }

  lazy val platform: Platform = scemplate.platformSpecific

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
      'F' -> 15
    ) foreach { case (k, v) => a(k) = v }

    a to ArraySeq
  }

  def hex(c: Char): Int = if (c < 128) HEX(c) else 0

  def unescape(s: String): String = {
    val buf = new StringBuilder
    val it = s.iterator

    def ch =
      if (it.hasNext) it.next()
      else sys.error("unescape: unexpected end of string")

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
              case c    => sys.error(s"unescape: non-escapable character: '$c' (${c.toInt})")
            })
        case c => buf += c
      }
    }

    buf.toString
  }

  def problem(pos: Int, parser: Parser, msg: String, line: Int, col: Int): Nothing = {
    val p = Position(pos, parser.input)
//    val offset = p.copy(line = p.line + line - 1, column = p.column + col - 1)

    Console.err.println(new RuntimeErrorFormatter(msg).customFormat(ParseError(p, p, Nil), parser.input))
    sys.exit(1)
  }

}
