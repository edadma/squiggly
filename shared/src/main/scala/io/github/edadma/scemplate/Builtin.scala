package io.github.edadma.scemplate

import io.github.edadma.cross_platform._

import io.github.edadma.datetime.Datetime

import scala.language.postfixOps

object Builtin {

  val namespaces: Map[String, Map[String, BuiltinFunction]] =
    Map(
      "images" -> Map(),
      "lang" -> Map()
    )
  val functions: Map[String, BuiltinFunction] =
    List(
      BuiltinFunction("anchorize", 1, {
        case (con, Seq(s: String)) =>
      }),
      BuiltinFunction("append", 2, {
        case (con, Seq(c: Seq[_], s: Seq[_]))                => s ++ c
        case (con, s: Seq[_]) if s.last.isInstanceOf[Seq[_]] => s.last.asInstanceOf[Seq[_]] ++ s.init
      }),
      // todo: https://gohugo.io/functions/base64/
      // todo: https://gohugo.io/functions/complement/
      BuiltinFunction(
        "contains",
        2, {
          case (con, Seq(elem: Any, s: Seq[_]))            => s contains elem
          case (con, Seq(elem: String, s: Map[String, _])) => s contains elem
          case (con, Seq(substr: String, s: String))       => s contains substr
        }
      ),
      BuiltinFunction(
        "default",
        2, {
          case (con, Seq(default: Any, input: Num))         => if (input != ZERO) input else default
          case (con, Seq(default: Any, input: String))      => if (input.nonEmpty) input else default
          case (con, Seq(default: Any, input: Iterable[_])) => if (input.nonEmpty) input else default
          case (con, Seq(_, input))                         => input
        }
      ),
      BuiltinFunction("drop", 2, { case (con, Seq(n: Num, s: Iterable[_])) => s drop n.toIntExact }),
      BuiltinFunction("fileExists", 1, { case (con, Seq(file: String))     => readableFile(file) }),
      BuiltinFunction("filter", 2, {
        case (con, Seq(NonStrictExpr(expr), s: Iterable[_])) => s filter (e => con.copy(data = e).beval(expr))
        case (con, Seq(s: String))                           => s // todo
      }),
      //BuiltinFunction("findRE", 2, { case (con, Seq(pattern: String, input: String)) => }), // todo: https://gohugo.io/functions/findre/
      // todo: https://gohugo.io/functions/getenv/
      // todo: https://gohugo.io/functions/group/
      // todo: https://gohugo.io/functions/highlight/
      // todo: https://gohugo.io/functions/hmac/
      BuiltinFunction("htmlEscape", 1, {
        case (con, Seq(s: String)) =>
          s replace ("&", "&amp;") replace ("<", "&lt;") replace (">", "&gt;") replace ("'", "&#39;") replace ("\"", "&#34;")
      }),
      // todo: htmlUnescape
      // todo: https://gohugo.io/functions/humanize/
      // todo: https://gohugo.io/functions/i18n/
      // todo: https://gohugo.io/functions/index-function/
      // todo: https://gohugo.io/functions/intersect/
      // todo: "join" https://gohugo.io/functions/delimit/
      // todo: https://gohugo.io/functions/jsonify/
      BuiltinFunction("length", 1, {
        case (con, Seq(s: String))      => s.length
        case (con, Seq(s: Iterable[_])) => s.size
      }),
      BuiltinFunction("map", 2, {
        case (con, Seq(NonStrictExpr(expr), s: Iterable[_])) => s map (e => con.copy(data = e).eval(expr))
        case (con, Seq(s: String))                           => s // todo
      }),
      BuiltinFunction("now", 0, _ => Datetime.now().timestamp),
      BuiltinFunction("number", 1, { case (con, Seq(s: String)) => BigDecimal(s) }),
      BuiltinFunction(
        "slice",
        2, {
          case (con, Seq(from: Num, s: Iterable[_]))             => s slice (from.toIntExact, s.size)
          case (con, Seq(from: Num, until: Num, s: Iterable[_])) => s slice (from.toIntExact, until.toIntExact)
          case (con, Seq(from: Num, s: String))                  => s slice (from.toIntExact, s.length)
          case (con, Seq(from: Num, until: Num, s: String))      => s slice (from.toIntExact, until.toIntExact)
        }
      ),
      BuiltinFunction("startsWith", 2, { case (con, Seq(prefix: String, s: String)) => s startsWith prefix }),
      BuiltinFunction("take", 2, { case (con, Seq(n: Num, s: Iterable[_]))          => s take n.toIntExact }),
      BuiltinFunction("trim", 1, { case (con, Seq(s: String))                       => s.trim }),
      BuiltinFunction("unix", 1, { case (con, Seq(d: Datetime))                     => BigDecimal(d.epochMillis) }),
    ) map (f => (f.name, f)) toMap

}

// todo: continue at https://gohugo.io/functions/findre/
