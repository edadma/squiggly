package io.github.edadma.squiggly

import io.github.edadma.char_reader.CharReader

import scala.util.{Failure, Success}
import org.parboiled2._

import scala.language.implicitConversions

class TagParser(val input: ParserInput,
                startpos: CharReader,
                startoffset: Int,
                functions: Map[String, TemplateFunction],
                namespaces: Map[String, Map[String, TemplateFunction]])
    extends Parser {
  val buf = new StringBuilder

  class Position(val offset: Int) {
    def pos: CharReader = {
      var p = startpos

      for (_ <- 1 to offset + startoffset) p = p.next

      p
    }

    def error(msg: String): Nothing = pos.error(msg)

    def shift(n: Int) = new Position(offset + n)

    override def toString: String = pos.toString
  }

  implicit def wsStr(s: String): Rule0 = rule(str(s) ~ sp)

  def kwcapture(s: String): Rule1[String] =
    rule(quiet(capture(str(s) ~ !CharPredicate.AlphaNum ~ sp) ~> ((s: String) => s.trim)))

  def kw(s: String): Rule0 = rule(quiet(str(s) ~ !CharPredicate.AlphaNum ~ sp))

  def sym(s: String): Rule1[String] = rule(quiet(capture(s) ~> ((s: String) => s.trim)))

  def tag: Rule1[TagParserAST] =
    rule {
      sp ~ (
        elseIfTag
          | defineTag
          | blockTag
          | matchTag
          | caseTag
          | endTag
          | withTag
          | forTag
          | elseTag
          | assignmentTag
          | returnTag
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
      conjunctive ~ zeroOrMore("or" ~ conjunctive ~> OrExpr)
    }

  def conjunctive: Rule1[ExprAST] =
    rule {
      not ~ zeroOrMore("and" ~ not ~> AndExpr)
    }

  def not: Rule1[ExprAST] =
    rule {
      kwcapture("not") ~ pos ~ not ~> PrefixExpr |
        comparitive
    }

  def comparitive: Rule1[ExprAST] =
    rule {
      pos ~ pipe ~ oneOrMore(
        (sym("<=") | sym(">=") | sym("!=") | sym("<") | sym(">") | sym("=") | kwcapture("div")) ~
          pos ~ pipe ~> Tuple3[String, Position, ExprAST] _) ~> CompareExpr |
        pipe
    }

  def pipe: Rule1[ExprAST] =
    rule {
      applicative ~ zeroOrMore(
        "|" ~ (apply | ident ~ push(Nil) ~> ApplyExpr) ~> PipeExpr
      )
    }

  def applicative: Rule1[ExprAST] = rule(apply | additive)

  def apply: Rule1[ApplyExpr] =
    rule(identnsp ~ test(cursorChar != '.' && cursorChar != '[') ~ sp ~ oneOrMore(additive) ~> ApplyExpr)

  def additive: Rule1[ExprAST] =
    rule {
      pos ~ multiplicative ~ oneOrMore((sym("++") | sym("+") | sym("-")) ~ pos ~ multiplicative ~> Tuple3[
        String,
        Position,
        ExprAST] _) ~> LeftInfixExpr | multiplicative
    }

  def multiplicative: Rule1[ExprAST] =
    rule {
      pos ~ negative ~ oneOrMore(
        (sym("*") | sym("/") | kwcapture("mod") | sym("\\")) ~
          pos ~ negative ~> Tuple3[String, Position, ExprAST] _) ~> LeftInfixExpr | negative
    }

  def negative: Rule1[ExprAST] =
    rule {
      sym("-") ~ pos ~ negative ~> PrefixExpr |
        power
    }

  def power: Rule1[ExprAST] =
    rule {
      pos ~ index ~ sym("^") ~ pos ~ power ~> RightInfixExpr |
        index
    }

  def index: Rule1[ExprAST] =
    rule {
      primary ~ zeroOrMore(
        "[" ~ pos ~ expression ~ "]" ~> IndexExpr | test(!lastChar.isWhitespace) ~ '.' ~ ident ~> MethodExpr)
    }

  def primary: Rule1[ExprAST] = rule {
    boolean |
      number |
      nul |
      variable |
      string |
      element |
      map |
      seq |
      "`" ~ expression ~ "`" ~> NonStrictExpr |
      "(" ~ expression ~ ")"
  }

  def map: Rule1[MapExpr] =
    rule(
      "{" ~ zeroOrMore(ident ~ ":" ~ pos ~ expression ~> Tuple3[TagParserIdent, Position, ExprAST] _)
        .separatedBy(",") ~ "}" ~> MapExpr)

  def seq: Rule1[SeqExpr] = rule("[" ~ zeroOrMore(expression).separatedBy(",") ~ "]" ~> SeqExpr)

  def number: Rule1[NumberExpr] = rule(pos ~ decimal ~> NumberExpr)

  def nul: Rule1[NullExpr] = rule(pos ~ "null" ~> NullExpr)

  def boolean: Rule1[BooleanExpr] =
    rule(pos ~ (kwcapture("true") | kwcapture("false")) ~> ((p: Position, b: String) => BooleanExpr(p, b == "true")))

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

  //  def nextIsMethod: Boolean = {
  //    buf.clear()
  //
  //    if (cursorChar.isLetter || cursorChar == '_') {
  //      buf += cursorChar
  //
  //      var i = 1
  //
  //      @tailrec
  //      def next(): Unit = {
  //        val c = charAtRC(i)
  //
  //        if (c.isLetterOrDigit || c == '_') {
  //          buf += c
  //          i += 1
  //          next()
  //        }
  //      }
  //
  //      next()
  //
  //      functions get buf.toString match {
  //        case Some(BuiltinFunction(_, arity, _)) if arity >= 1 => true
  //        case _                                                => false
  //      }
  //    } else false
  //  }

  def element: Rule1[ElementExpr] =
    rule(
      pos ~ capture(optional('$')) ~ '.' ~ zeroOrMore( /*test(!nextIsMethod) ~*/ identnsp)
        .separatedBy('.') ~ sp ~> ElementExpr)

  def string: Rule1[StringExpr] =
    rule(pos ~ (singleQuoteString | doubleQuoteString) ~> ((p: Position, s: String) => StringExpr(p, s)))

  def singleQuoteString: Rule1[String] = rule('\'' ~ capture(zeroOrMore("\\'" | noneOf("'\n"))) ~ '\'' ~ sp)

  def doubleQuoteString: Rule1[String] = rule('"' ~ capture(zeroOrMore("\\\"" | noneOf("\"\n"))) ~ '"' ~ sp)

  def identnsp: Rule1[TagParserIdent] =
    rule {
      pos ~ !("if" | "true" | "false" | "null" | "elsif" | "switch" | "unless" | "define" | "block" | "match" | "case" | "no" ~ "output") ~ capture(
        (CharPredicate.Alpha | '_') ~ zeroOrMore(CharPredicate.AlphaNum | '_')) ~> TagParserIdent
    }

  def ident: Rule1[TagParserIdent] = rule(identnsp ~ sp)

  def pos: Rule1[Position] = rule(push(new Position(cursor)))

  def sp: Rule0 = rule(quiet(zeroOrMore(anyOf(" \t\r\n"))))

  def defineTag: Rule1[DefineAST] = rule(pos ~ "define" ~ ident ~> DefineAST)

  def blockTag: Rule1[BlockAST] = rule(pos ~ "block" ~ ident ~ expression ~> BlockAST)

  def assignmentTag: Rule1[AssignmentAST] = rule(ident ~ ":=" ~ expression ~> AssignmentAST)

  def returnTag: Rule1[ReturnAST] = rule("return" ~ optional(expression) ~> ReturnAST)

  def ifTag: Rule1[IfAST] = rule(pos ~ "if" ~ condition ~> IfAST)

  def elseIfTag: Rule1[ElseIfAST] = rule(pos ~ "elsif" ~ condition ~> ElseIfAST)

  def elseTag: Rule1[ElseAST] = rule(pos ~ "else" ~> ElseAST)

  def endTag: Rule1[EndAST] = rule(pos ~ "end" ~> EndAST)

  def matchTag: Rule1[MatchAST] = rule(pos ~ "match" ~ condition ~> MatchAST)

  def caseTag: Rule1[CaseAST] = rule(pos ~ "case" ~ condition ~> CaseAST)

  def withTag: Rule1[WithAST] = rule(pos ~ "with" ~ expression ~> WithAST)

  def forIndex: Rule1[(TagParserIdent, Option[TagParserIdent])] =
    rule(ident ~ optional("," ~ ident) ~ "<-" ~> Tuple2[TagParserIdent, Option[TagParserIdent]] _)

  def forTag: Rule1[ForAST] =
    rule(kw("for") ~ optional(forIndex) ~ pos ~ expression ~> ForAST)

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
