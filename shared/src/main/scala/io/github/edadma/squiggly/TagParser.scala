package io.github.edadma.squiggly

import scala.util.parsing.combinator.{ImplicitConversions, PackratParsers}
import scala.util.parsing.combinator.syntactical.StandardTokenParsers
import scala.util.parsing.input.CharSequenceReader

object TagParser extends StandardTokenParsers with PackratParsers with ImplicitConversions:
  override val lexical = new TagLexer

  /** Parse the contents of a `{{ ... }}` tag.  Outer template position tracking
    * (across the whole template source) lives in `TemplateParser`; this parser
    * only concerns itself with the local span of the tag.
    */
  def parse(input: String): TagParserAST =
    if input.trim.startsWith("//") then CommentAST(input.trim.drop(2).trim)
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
      |""".trim.stripMargin.split("\\s+"))
  lexical.delimiters ++= ("+ ++ - * / \\ ^ % ( ) [ ] { } ` | . , < <= > >= != = $ : <- :=" split ' ') :+ " ."

  type P[+T] = PackratParser[T]

  // Reserved-word-led tags (forTag, elseTag, endTag, elsifTag, withTag, matchTag, caseTag,
  // defineTag, blockTag, returnTag) are unambiguous because their leading keywords cannot
  // start an expression. `assignmentTag` (`ident := expr`) must come before `expression`
  // so that a leading identifier doesn't get partial-matched as a VarExpr expression with
  // `:= ...` left over. `ifTag` (the block-start `{{ if x }}` form) must come AFTER
  // `expression` so that `if x then a else b` is captured as a ConditionalAST instead of
  // being cut to `IfAST(x)` with `then a else b` orphaned.
  lazy val tag: P[TagParserAST] =
    forTag | elseTag | endTag | elsifTag | withTag | matchTag | caseTag |
      defineTag | blockTag | returnTag |
      assignmentTag | expression | ifTag

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

  // KNOWN LIMITATION — `id[expr]` (subscript on a bare variable) and
  // `id [list]` (paren-less call passing a list literal) are
  // token-identical after the lexer strips whitespace. The grammar
  // resolves ambiguity by always trying `apply` first, so a bare-var
  // subscript like `xs[0]` parses as a function call `apply(xs, [0])`
  // and the evaluator reports "function not found: xs". To subscript a
  // bare-var assignment, either:
  //
  //   - put the binding inside the indexable chain — `.foo.bar[0]`
  //     starts with `.`, so the parser routes through `index` directly;
  //   - wrap the variable in parens — `(xs)[0]` parses primary as the
  //     parenthesised expression, then `index` chains the subscript;
  //   - iterate with an indexed `for` — `for x, i <- xs; if i = 0 ...`
  //     — and let the loop body capture the head element.
  //
  // Distinguishing the two cases properly requires a reader-position
  // check (no-whitespace adjacency) since StandardTokenParsers discards
  // whitespace before reaching this rule. Tracked for a future fix.
  lazy val apply: P[ApplyExpr] = identifier ~ (guard(not(".")) ~> rep1(additive)) ^^ ApplyExpr.apply

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

  // Index / method-access chains: `primary` followed by any number of `[expr]`
  // or `.id` suffixes. Folded left-to-right so `'hi'.reverse.upper` becomes
  // `MethodExpr(MethodExpr(StringExpr("hi"), reverse), upper)`.
  lazy val index: P[ExprAST] = positioned(
    primary ~ rep(indexSuffix) ^^ { case base ~ ops =>
      ops.foldLeft(base)((lhs, op) => op(lhs))
    },
  )

  private lazy val indexSuffix: P[ExprAST => ExprAST] =
    ("[" ~> expression <~ "]") ^^ (idx => (lhs: ExprAST) => IndexExpr(lhs, idx))
      | ("." ~> identifier) ^^ (id => (lhs: ExprAST) => MethodExpr(lhs, id))

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

  lazy val defineTag: P[DefineAST] = "define" ~> identifier ^^ DefineAST.apply

  lazy val blockTag: P[BlockAST] = "block" ~> identifier ~ expression ^^ BlockAST.apply

  lazy val returnTag: P[ReturnAST] = "return" ~> opt(expression) ^^ ReturnAST.apply
