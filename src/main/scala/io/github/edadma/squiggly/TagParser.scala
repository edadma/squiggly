package io.github.edadma.squiggly

import io.github.edadma.char_reader.CharReader

import scala.util.parsing.combinator.{ImplicitConversions, PackratParsers}
import scala.util.parsing.combinator.lexical.StdLexical
import scala.util.parsing.combinator.syntactical.StandardTokenParsers
import scala.util.parsing.input.CharSequenceReader

object TagParser extends StandardTokenParsers with PackratParsers with ImplicitConversions:
  override val lexical = new TagLexer

  def parse(input: String, startpos: CharReader, startoffset: Int): TagParserAST =
    if input.trim startsWith "//" then CommentAST(input.trim drop 2 trim)
    else
      phrase(tag)(new lexical.Scanner(new PackratReader(new CharSequenceReader(input)))) match
        case Success(ast, _) => ast
        case e: NoSuccess    => sys.error(s"parse error: $e")

  lexical.reserved ++= ("""
      |if
      |then
      |else
      |div
      |and
      |or
      |not
      |mod
      |define
      |block
      |return
      |elsif
      |end
      |match
      |case
      |with
      |for
      |true
      |false
      |null
      |""".trim.stripMargin split "\\s+")
  lexical.delimiters ++= ("+ ++ - * / \\ ^ % ( ) [ ] { } ` | . , < <= > >= != = $ : <- :=" split ' ') :+ " ."

  type P[+T] = PackratParser[T]

  lazy val tag: P[TagParserAST] =
    forTag | elseTag | endTag | ifTag | elsifTag | withTag | matchTag | caseTag | assignmentTag | expression

  lazy val identifier: P[Ident] = positioned(ident ^^ Ident.apply)

  lazy val expression: P[ExprAST] = conditional

  lazy val conditional: P[ExprAST] = positioned(
    ("if" ~> condition <~ "then") ~ conditional ~ opt("else" ~> conditional) ^^ ConditionalAST.apply
      | disjunctive,
  )

  lazy val condition: P[ExprAST] = disjunctive

  lazy val disjunctive: P[ExprAST] = positioned(
    disjunctive ~ ("or" ~> conjunctive) ^^ OrExpr.apply
      | conjunctive,
  )

  lazy val conjunctive: P[ExprAST] = positioned(
    conjunctive ~ ("and" ~> complement) ^^ AndExpr.apply
      | complement,
  )

  lazy val complement: P[ExprAST] = positioned(
    "not" ~ relational ^^ PrefixExpr.apply
      | relational,
  )

  lazy val relational: P[ExprAST] = positioned(
    pipe ~ rep1(("<" | ">" | "<=" | ">=" | "=" | "!=" | "div") ~ pipe ^^ Tuple2.apply) ^^ CompareExpr.apply
      | pipe,
  )

  lazy val pipe: P[ExprAST] = positioned(
    applicative ~ ("|" ~> (apply | identifier ^^ (n => ApplyExpr(n, Nil)))) ^^ PipeExpr.apply
      | applicative,
  )

  lazy val applicative: P[ExprAST] = apply | additive

  lazy val apply: P[ApplyExpr] = identifier ~ rep1(additive) ^^ ApplyExpr.apply

  lazy val additive: P[ExprAST] = positioned(
    additive ~ ("++" | "+" | "-") ~ multiplicative ^^ LeftInfixExpr.apply
      | multiplicative,
  )

  lazy val multiplicative: P[ExprAST] = positioned(
    multiplicative ~ ("*" | "/" | "\\" | "mod") ~ negative ^^ LeftInfixExpr.apply
      | negative,
  )

  lazy val negative: P[ExprAST] = positioned(
    "-" ~ negative ^^ PrefixExpr.apply
      | exponentiation,
  )

  lazy val exponentiation: P[ExprAST] = positioned(
    index ~ "^" ~ negative ^^ RightInfixExpr.apply
      | index,
  )

  lazy val index: P[ExprAST] = positioned(
    primary ~ ("[" ~> expression <~ "]") ^^ IndexExpr.apply
      | primary ~ ("." ~> identifier) ^^ MethodExpr.apply
      | primary,
  )

  lazy val decimal: P[BigDecimal] = numericLit ^^ BigDecimal.apply

  lazy val primary: P[ExprAST] = positioned(
    "true" ^^^ BooleanExpr(true)
      | "false" ^^^ BooleanExpr(false)
      | "null" ^^^ NullExpr()
      | global ~ identifier ^^ VarExpr.apply
      | global ~ ((" ." | ".") ~> repsep(identifier, ".")) ^^ ElementExpr.apply
      | decimal ^^ NumberExpr.apply
      | stringLit ^^ StringExpr.apply
      | "{" ~> repsep(identifier ~ (":" ~> expression) ^^ Tuple2.apply, ",") <~ "}" ^^ MapExpr.apply
      | "[" ~> repsep(expression, ",") <~ "]" ^^ SeqExpr.apply
      | "`" ~> expression <~ "`" ^^ NonStrictExpr.apply
      | "(" ~> expression <~ ")",
  )

  lazy val global: P[String] = opt("$") ^^ (_ getOrElse "")

  lazy val forTag: P[ForAST] =
    "for" ~> opt(identifier ~ opt("," ~> identifier) <~ "<-" ^^ Tuple2.apply) ~ expression ^^ ForAST.apply

  lazy val endTag: P[EndAST] = "end" ^^^ EndAST()

  lazy val elseTag: P[ElseAST] = "else" ^^^ ElseAST()

  lazy val ifTag: P[IfAST] = "if" ~> condition ^^ IfAST.apply

  lazy val elsifTag: P[ElseIfAST] = "elsif" ~> condition ^^ ElseIfAST.apply

  lazy val withTag: P[WithAST] = "with" ~> expression ^^ WithAST.apply

  lazy val matchTag: P[MatchAST] = "match" ~> expression ^^ MatchAST.apply

  lazy val caseTag: P[CaseAST] = "case" ~> expression ^^ CaseAST.apply

  lazy val assignmentTag: P[AssignmentAST] = ident ~ (":=" ~> expression) ^^ AssignmentAST.apply
