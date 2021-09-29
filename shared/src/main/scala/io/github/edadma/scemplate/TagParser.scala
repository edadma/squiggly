package io.github.edadma.scemplate

import scala.util.{Failure, Success}
import org.parboiled2._

import scala.annotation.tailrec
import scala.language.implicitConversions

class TagParser(val input: ParserInput,
                line: Int,
                col: Int,
                functions: Map[String, BuiltinFunction],
                namespaces: Map[String, Map[String, BuiltinFunction]])
    extends Parser {
  val buf = new StringBuilder

  implicit def wsStr(s: String): Rule0 = rule(str(s) ~ sp)

  def kw(s: String): Rule1[String] = rule(quiet(capture(s) ~> ((s: String) => s.trim)))

  def tag: Rule1[TagParserAST] =
    rule {
      sp ~ (
        elseIfTag
          | endTag
          | withTag
          | forTag
          | elseTag
          | assignmentTag
          | commentTag
          | expression
          | ifTag
      ) ~ EOI
    }

  def expression: Rule1[ExprAST] = conditional

  def conditional: Rule1[ExprAST] =
    rule {
      ("if" ~ condition ~ "then" ~ conditional ~ optional("else" ~ conditional) ~> ConditionalAST) |
        condition
    }

  def condition: Rule1[ExprAST] = disjunctive

  def disjunctive: Rule1[ExprAST] =
    rule {
      conjunctive ~ zeroOrMore(
        kw("or") ~ conjunctive ~> BinaryExpr
      )
    }

  def conjunctive: Rule1[ExprAST] =
    rule {
      not ~ zeroOrMore(
        kw("and") ~ not ~> BinaryExpr
      )
    }

  def not: Rule1[ExprAST] =
    rule {
      kw("not") ~ not ~> UnaryExpr |
        comparitive
    }

  def comparitive: Rule1[ExprAST] =
    rule {
      pipe ~ oneOrMore(
        (kw("<=") | kw(">=") | kw("!=") | kw("<") | kw(">") | kw("=")) ~
          pipe ~> Tuple2[String, ExprAST] _) ~> CompareExpr |
        pipe
    }

  def pipe: Rule1[ExprAST] =
    rule {
      applicative ~ zeroOrMore(
        "|" ~ (apply | ident ~ push(Nil) ~> ApplyExpr) ~> PipeExpr
      )
    }

  def applicative: Rule1[ExprAST] = rule(apply | additive)

  def apply: Rule1[ApplyExpr] = rule(identnsp ~ test(cursorChar != '.') ~ sp ~ oneOrMore(additive) ~> ApplyExpr)

  def additive: Rule1[ExprAST] =
    rule {
      multiplicative ~ zeroOrMore(
        kw("+") ~ multiplicative ~> BinaryExpr
          | kw("-") ~ multiplicative ~> BinaryExpr
      )
    }

  def multiplicative: Rule1[ExprAST] =
    rule {
      negative ~ zeroOrMore(
        kw("*") ~ negative ~> BinaryExpr
          | kw("/") ~ negative ~> BinaryExpr
          | kw("mod") ~ negative ~> BinaryExpr
          | kw("\\") ~ negative ~> BinaryExpr)
    }

  def negative: Rule1[ExprAST] =
    rule {
      kw("-") ~ negative ~> UnaryExpr |
        power
    }

  def power: Rule1[ExprAST] =
    rule {
      method ~ kw("^") ~ power ~> BinaryExpr |
        method
    }

  def method: Rule1[ExprAST] =
    rule {
      primary ~ test(cursor == 0 || !lastChar.isWhitespace) ~ '.' ~ (identnsp ~ test(cursorChar != '.') ~ sp ~
        oneOrMore(primary) | ident ~ push(Nil)) ~> MethodExpr |
        primary
    }

  def primary: Rule1[ExprAST] = rule {
    boolean |
      number |
      variable |
      string |
      element |
      map |
      seq |
      "<" ~ expression ~ ">" ~> NonStrictExpr |
      "(" ~ expression ~ ")"
  }

  def map: Rule1[MapExpr] =
    rule("{" ~ zeroOrMore(ident ~ ":" ~ expression ~> Tuple2[Ident, ExprAST] _).separatedBy(",") ~ "}" ~> MapExpr)

  def seq: Rule1[SeqExpr] = rule("[" ~ zeroOrMore(expression).separatedBy(",") ~ "]" ~> SeqExpr)

  def number: Rule1[NumberExpr] = rule(pos ~ decimal ~> NumberExpr)

  def boolean: Rule1[BooleanExpr] =
    rule(pos ~ (kw("true") | kw("false")) ~> ((p: Int, b: String) => BooleanExpr(p, b == "true")))

  def decimal: Rule1[BigDecimal] =
    rule {
      capture(
        (zeroOrMore(CharPredicate.Digit) ~ '.' ~ digits | digits ~ '.') ~
          optional((ch('e') | 'E') ~ optional(ch('+') | '-') ~ digits) |
          digits
      ) ~ sp ~> ((s: String) => BigDecimal(s))
    }

  def integer: Rule1[Int] = rule(capture(digits) ~ sp ~> ((s: String) => s.toInt))

  def digits: Rule0 = rule(oneOrMore(CharPredicate.Digit))

  def variable: Rule1[VarExpr] = rule(pos ~ capture(optional('$')) ~ ident ~> VarExpr)

  def nextIsMethod: Boolean = {
    buf.clear()

    if (cursorChar.isLetter || cursorChar == '_') {
      buf += cursorChar

      var i = 1

      @tailrec
      def next(): Unit = {
        val c = charAtRC(i)

        if (c.isLetterOrDigit || c == '_') {
          buf += c
          i += 1
          next()
        }
      }

      next()

      functions get buf.toString match {
        case Some(BuiltinFunction(_, arity, _)) if arity >= 1 => true
        case _                                                => false
      }
    } else false
  }

  def element: Rule1[ElementExpr] =
    rule(
      pos ~ capture(optional('$')) ~ '.' ~ zeroOrMore(test(!nextIsMethod) ~ identnsp)
        .separatedBy('.') ~ sp ~> ElementExpr)

  def string: Rule1[StringExpr] =
    rule(pos ~ (singleQuoteString | doubleQuoteString) ~> ((p: Int, s: String) => StringExpr(p, unescape(s))))

  def backtickString: Rule1[String] = rule(capture('`' ~ zeroOrMore("\\`" | noneOf("`"))) ~ '`' ~ sp)

  def singleQuoteString: Rule1[String] = rule('\'' ~ capture(zeroOrMore("\\'" | noneOf("'\n"))) ~ '\'' ~ sp)

  def doubleQuoteString: Rule1[String] = rule('"' ~ capture(zeroOrMore("\\\"" | noneOf("\"\n"))) ~ '"' ~ sp)

  def identnsp: Rule1[Ident] =
    rule {
      pos ~ !"if" ~ !"true" ~ !"false" ~ capture((CharPredicate.Alpha | '_') ~ zeroOrMore(CharPredicate.AlphaNum | '_')) ~> Ident
    }

  def ident: Rule1[Ident] = rule(identnsp ~ sp)

  def pos: Rule1[Int] = rule(push(cursor))

  def sp: Rule0 = rule(quiet(zeroOrMore(anyOf(" \t\r\n"))))

  def assignmentTag: Rule1[AssignmentAST] = rule(ident ~ ":=" ~ expression ~> AssignmentAST)

  def ifTag: Rule1[IfAST] = rule(pos ~ "if" ~ condition ~> IfAST)

  def elseIfTag: Rule1[ElseIfAST] = rule(pos ~ "else" ~ "if" ~ condition ~> ElseIfAST)

  def elseTag: Rule1[ElseAST] = rule(pos ~ "else" ~> ElseAST)

  def endTag: Rule1[EndAST] = rule(pos ~ "end" ~> EndAST)

  def withTag: Rule1[WithAST] = rule(pos ~ "with" ~ expression ~> WithAST)

  def index: Rule1[(Option[Ident], Ident)] =
    rule(optional(ident ~ ",") ~ ident ~ "<-" ~> Tuple2[Option[Ident], Ident] _)

  def forTag: Rule1[ForAST] =
    rule(pos ~ "for" ~ optional(index) ~ expression ~> ForAST)

  def commentTag: Rule1[CommentAST] =
    rule("/*" ~ capture(zeroOrMore(!(sp ~ str("*/")) ~ ANY)) ~ sp ~ "*/" ~> CommentAST)

  def parseTag: TagParserAST =
    tag.run() match {
      case Success(ast)           => ast
      case Failure(e: ParseError) =>
        //        val p = e.position
        //        val poffset = p.copy(line = p.line + line - 1, column = p.column + col - 1)
        //        val pp = e.principalPosition
        //        val ppoffset = pp.copy(line = pp.line + line - 1, column = pp.column + col - 1)
        //        val eoffset = e.copy(position = poffset, principalPosition = ppoffset)
        //
        //        sys.error(formatError(eoffset))
        sys.error(formatError(e))
      case Failure(e) => sys.error("Unexpected error during parsing run: " + e)
    }

  def parseExpression: ExprAST =
    expression.run() match {
      case Success(ast)           => ast
      case Failure(e: ParseError) => sys.error("Expression is not valid: " + formatError(e))
      case Failure(e)             => sys.error("Unexpected error during parsing run: " + e)
    }

}
