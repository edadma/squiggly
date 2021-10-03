package io.github.edadma.squiggly

import io.github.edadma.cross_platform._

import io.github.edadma.datetime.Datetime

import scala.language.postfixOps

object Builtin {

  val namespaces: Map[String, Map[String, BuiltinFunction]] =
    Map(
      "images" -> Map(),
      "lang" -> Map(),
      "math" -> Map(),
      "path" -> Map(),
      "strings" -> Map()
    )
  val functions: Map[String, BuiltinFunction] =
    List(
//      BuiltinFunction("anchorize", 1, {
//        case (con, Seq(s: String)) =>
//      }),
      BuiltinFunction("append", 2, {
        case (con, Seq(c: Seq[_], s: Seq[_]))                => s ++ c
        case (con, s: Seq[_]) if s.last.isInstanceOf[Seq[_]] => s.last.asInstanceOf[Seq[_]] ++ s.init
      }),
      // todo: https://gohugo.io/functions/base64/
      // todo: https://gohugo.io/functions/complement/
      BuiltinFunction(
        "contains",
        2, {
          case (con, Seq(elem: Any, s: Seq[_]))       => s contains elem
          case (con, Seq(elem: String, m: Map[_, _])) => m.asInstanceOf[Map[String, _]] contains elem
          case (con, Seq(substr: String, s: String))  => s contains substr
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
      BuiltinFunction("distinct", 1, { case (con, Seq(s: Seq[_])) => s.distinct }),
      BuiltinFunction("drop", 2, {
        case (con, Seq(n: Num, s: Iterable[_])) => s drop n.toIntExact
        case (con, Seq(n: Num, s: String))      => s drop n.toIntExact
      }),
      BuiltinFunction("dropRight", 2, {
        case (con, Seq(n: Num, s: Iterable[_])) => s dropRight n.toIntExact
        case (con, Seq(n: Num, s: String))      => s dropRight n.toIntExact
      }),
      BuiltinFunction("fileExists", 1, { case (con, Seq(file: String)) => readableFile(file) }),
      BuiltinFunction("filter", 2, {
        case (con, Seq(NonStrictExpr(expr), s: Iterable[_])) => s filter (e => con.copy(data = e).beval(expr))
      }),
      BuiltinFunction("filterNot", 2, {
        case (con, Seq(NonStrictExpr(expr), s: Iterable[_])) => s filterNot (e => con.copy(data = e).beval(expr))
      }),
      //BuiltinFunction("findRE", 2, { case (con, Seq(pattern: String, input: String)) => }), // todo: https://gohugo.io/functions/findre/
      // todo: https://gohugo.io/functions/format/ https://gohugo.io/functions/dateformat/
      // todo: https://gohugo.io/functions/getenv/
      // todo: https://gohugo.io/functions/group/
      // todo: https://gohugo.io/functions/highlight/
      // todo: https://gohugo.io/functions/hmac/
      BuiltinFunction("htmlEscape", 1, {
        case (con, Seq(s: String)) =>
          s replace ("&", "&amp;") replace ("<", "&lt;") replace (">", "&gt;") replace ("'", "&apos;") replace ("\"", "&quot;")
      }),
      // todo: htmlUnescape
      // todo: https://gohugo.io/functions/humanize/
      // todo: https://gohugo.io/functions/i18n/
      // todo: https://gohugo.io/functions/intersect/
      // todo: "join" https://gohugo.io/functions/delimit/
      // todo: https://gohugo.io/functions/jsonify/
      BuiltinFunction("length", 1, {
        case (con, Seq(s: String))      => s.length
        case (con, Seq(s: Iterable[_])) => s.size
      }),
      BuiltinFunction("lower", 1, { case (con, Seq(s: String)) => s.toLowerCase }),
      BuiltinFunction("map", 2, {
        case (con, Seq(NonStrictExpr(expr), s: Iterable[_])) => s map (e => con.copy(data = e).eval(expr))
        case (con, Seq(s: String))                           => s // todo: map named function
      }),
      // todo: https://gohugo.io/functions/markdownify/
      // todo: https://gohugo.io/functions/md5/
      // todo: https://gohugo.io/functions/merge/
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
      // todo: https://gohugo.io/functions/path.base/
      // todo: https://gohugo.io/functions/pluralize/
      BuiltinFunction("partial", 1, {
        case (con, Seq(path: String))            => partial(con, path, null)
        case (con, Seq(path: String, data: Any)) => partial(con, path, data)
      }),
      // todo: https://gohugo.io/functions/querify/
      // todo: https://gohugo.io/functions/readdir/
      // todo: https://gohugo.io/functions/replace/
      // todo: https://gohugo.io/functions/replaceRE/
      // todo: https://gohugo.io/functions/sha/
      // todo: https://gohugo.io/functions/shuffle/
      // todo: https://gohugo.io/functions/singularize/
      // todo: https://gohugo.io/functions/sort/
      BuiltinFunction("split", 2, { case (con, Seq(delim: String, s: String)) => s split delim toSeq }),
      BuiltinFunction("substring", 3, {
        case (con, Seq(start: BigDecimal, end: BigDecimal, s: String)) => s.substring(start.toIntExact, end.toIntExact)
      }),
      BuiltinFunction("startsWith", 2, { case (con, Seq(prefix: String, s: String)) => s startsWith prefix }),
      // todo: https://gohugo.io/functions/strings.count/
      BuiltinFunction("take", 2, {
        case (con, Seq(n: Num, s: Iterable[_])) => s take n.toIntExact
        case (con, Seq(n: Num, s: String))      => s take n.toIntExact
      }),
      // todo: https://gohugo.io/functions/symdiff/
      BuiltinFunction("takeRight", 2, {
        case (con, Seq(n: Num, s: Iterable[_])) => s takeRight n.toIntExact
        case (con, Seq(n: Num, s: String))      => s takeRight n.toIntExact
      }),
      BuiltinFunction("time", 1, { case (con, Seq(s: String)) => Datetime.fromString(s) }),
      // todo: https://gohugo.io/functions/title/
      BuiltinFunction("toString", 1, { case (con, Seq(a: Any)) => a.toString }),
      BuiltinFunction("trim", 1, { case (con, Seq(s: String))  => s.trim }), // todo: https://gohugo.io/functions/trim/
      // todo: https://gohugo.io/functions/truncate/
      // todo: https://gohugo.io/functions/union/
      BuiltinFunction("unix", 1, { case (con, Seq(d: Datetime)) => BigDecimal(d.epochMillis) }),
      // todo: https://gohugo.io/functions/transform.unmarshal/
      BuiltinFunction("upper", 1, { case (con, Seq(s: String)) => s.toUpperCase }),
      // todo: https://gohugo.io/functions/urlize/
    ) map (f => (f.name, f)) toMap

  private def partial(context: Context, path: String, data: Any): Any = {
    val partial = context.renderer.partials(path) getOrElse sys.error(s"partial '$path' count not be loaded")

    context.renderer.render(data, partial, context.out)
  }

}
