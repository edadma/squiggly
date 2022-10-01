package io.github.edadma.squiggly

import scala.util.parsing.combinator.Parsers
import scala.util.parsing.combinator.lexical.StdLexical
import scala.util.parsing.input.CharSequenceReader.EofCh

class TagLexer extends StdLexical:

  private def digits = rep1(digit) ^^ (_.mkString)

  private def exponent = (elem('e') | 'E') ~ opt(elem('+') | '-') ~ digits ^^ {
    case e ~ None ~ exp    => List(e, exp).mkString
    case e ~ Some(s) ~ exp => List(e, s, exp).mkString
  }

  private def optExponent = opt(exponent) ^^ {
    case None    => ""
    case Some(e) => e
  }

  override def token: Parser[Token] =
    identChar ~ rep(identChar | digit) ^^ { case first ~ rest => processIdent(first :: rest mkString "") }
      | opt(digits) ~ '.' ~ digits ~ optExponent ^^ {
        case Some(intPart) ~ _ ~ fracPart ~ exp => NumericLit(s"$intPart.$fracPart$exp")
        case None ~ _ ~ fracPart ~ exp          => NumericLit(s".$fracPart$exp")
      }
      | digits ~ optExponent ^^ { case intPart ~ exp =>
        NumericLit(s"$intPart$exp")
      }
      | (elem('\'') | '"') >> { c =>
        rep(
          guard(not(c | elem('\n'))) ~> (('\\' ~> accept(
            "escape character",
            {
              case c @ ('\'' | '"') => c
              case 'b'              => '\b'
              case 'f'              => '\f'
              case 'n'              => '\n'
              case 'r'              => '\r'
              case 't'              => '\t'
            },
          )) | ('\\' ~> 'u' ~> (repN(
            4,
            digit | 'a' | 'A' | 'b' | 'B' | 'c' | 'C' | 'd' | 'D' | 'e' | 'E' | 'f' | 'F',
          ) ^^ (ds => Integer.parseInt(ds.mkString, 16).toChar))) | chrExcept('\\')),
        ) <~ c
      } ^^ (l => StringLit(l.mkString))
      | EofCh ^^^ EOF
      | delim
      | failure("illegal character")
