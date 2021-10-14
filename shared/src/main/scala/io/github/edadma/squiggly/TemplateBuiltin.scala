package io.github.edadma.squiggly

import java.math.{MathContext, RoundingMode}
import io.github.edadma.cross_platform._
import io.github.edadma.datetime.{Datetime, DatetimeFormatter}

import scala.collection.mutable.ListBuffer
import scala.language.postfixOps
import scala.util.Random

object TemplateBuiltin {

  val DATE_FULL_FORMAT: DatetimeFormatter = DatetimeFormatter("WWWW, MMMM D, Y")
  val DATE_LONG_FORMAT: DatetimeFormatter = DatetimeFormatter("MMMM D, Y")
  val DATE_MEDIUM_FORMAT: DatetimeFormatter = DatetimeFormatter("MMM D, Y")
  val DATE_SHORT_FORMAT: DatetimeFormatter = DatetimeFormatter("M/D/YY")

  val namespaces: Map[String, Map[String, TemplateFunction]] =
    Map(
      "images" -> Map(),
      "lang" -> Map(),
      "math" -> Map(),
      "path" -> Map(),
      "strings" -> Map()
    )
  val functions: Map[String, TemplateFunction] =
    List(
      TemplateFunction("abs", 1, { case (con, Seq(n: BigDecimal)) => n.abs }),
      //      BuiltinFunction("anchorize", 1, {
      //        case (con, Seq(s: String)) =>
      //      }),
      TemplateFunction("append", 2, {
        case (con, Seq(c: Seq[_], s: Seq[_]))                => s ++ c
        case (con, s: Seq[_]) if s.last.isInstanceOf[Seq[_]] => s.last.asInstanceOf[Seq[_]] ++ s.init
      }),
      // todo: https://gohugo.io/functions/base64/
      TemplateFunction("capitalize", 1, {
        case (con, Seq(s: String)) =>
          if (s.nonEmpty) s.head.toUpper +: s.tail.toLowerCase
          else s
      }),
      TemplateFunction("ceil", 1, {
        case (con, Seq(n: BigDecimal)) => n.round(new MathContext(n.mc.getPrecision, RoundingMode.CEILING))
      }),
      TemplateFunction("compact", 1, { case (con, Seq(s: Seq[_])) => s.filterNot(e => e == () || e == null) }),
      // todo: https://gohugo.io/functions/complement/
      TemplateFunction(
        "contains",
        2, {
          case (con, Seq(elem: Any, s: Seq[_]))       => s contains elem
          case (con, Seq(elem: String, m: Map[_, _])) => m.asInstanceOf[Map[String, _]] contains elem
          case (con, Seq(substr: String, s: String))  => s contains substr
        }
      ),
      TemplateFunction(
        "default",
        2, {
          case (con, Seq(default: Any, () | "" | `ZERO`))   => default
          case (con, Seq(default: Any, input: Iterable[_])) => if (input.nonEmpty) input else default
          case (con, Seq(_, input))                         => input
        }
      ),
      TemplateFunction("distinct", 1, { case (con, Seq(s: Seq[_])) => s.distinct }),
      TemplateFunction("drop", 2, {
        case (con, Seq(n: Num, s: Iterable[_])) => s drop n.toIntExact
        case (con, Seq(n: Num, s: String))      => s drop n.toIntExact
      }),
      TemplateFunction("dropRight", 2, {
        case (con, Seq(n: Num, s: Iterable[_])) => s dropRight n.toIntExact
        case (con, Seq(n: Num, s: String))      => s dropRight n.toIntExact
      }),
      TemplateFunction("fileExists", 1, { case (con, Seq(file: String)) => readableFile(file) }),
      TemplateFunction("filter", 2, {
        case (con, Seq(NonStrictExpr(expr), s: Iterable[_])) => s filter (e => con.copy(data = e).beval(expr))
      }),
      TemplateFunction("filterNot", 2, {
        case (con, Seq(NonStrictExpr(expr), s: Iterable[_])) => s filterNot (e => con.copy(data = e).beval(expr))
      }),
      TemplateFunction(
        "findRE",
        2, {
          case (con, Seq(pattern: String, input: String)) => findRE(pattern, input) toList
          case (con, Seq(pattern: String, input: String, limit: Num)) =>
            findRE(pattern, input) take limit.toIntExact toList
        }
      ),
      TemplateFunction(
        "format",
        2, {
          case (con, Seq(":date_full", datetime: Datetime))   => DATE_FULL_FORMAT.format(datetime)
          case (con, Seq(":date_long", datetime: Datetime))   => DATE_LONG_FORMAT.format(datetime)
          case (con, Seq(":date_medium", datetime: Datetime)) => DATE_MEDIUM_FORMAT.format(datetime)
          case (con, Seq(":date_short", datetime: Datetime))  => DATE_SHORT_FORMAT.format(datetime)
          case (con, Seq(format: String, datetime: Datetime)) => datetime format format
        }
      ),
      // todo: https://gohugo.io/functions/getenv/
      // todo: https://gohugo.io/functions/group/
      // todo: https://gohugo.io/functions/highlight/
      // todo: https://gohugo.io/functions/hmac/
      TemplateFunction("htmlEscape", 1, {
        case (con, Seq(s: String)) =>
          s replace ("&", "&amp;") replace ("<", "&lt;") replace (">", "&gt;") replace ("'", "&apos;") replace ("\"", "&quot;")
      }),
      // todo: htmlUnescape
      // todo: https://gohugo.io/functions/humanize/
      // todo: https://gohugo.io/functions/i18n/
      // todo: https://gohugo.io/functions/intersect/
      TemplateFunction(
        "join",
        1, {
          case (con, Seq(delim: String, s: Seq[_])) => s mkString delim
          case (con, Seq(delim: String, last: String, s: Seq[_])) =>
            if (s.length >= 2) s.init.mkString(delim) ++ last ++ s.last.toString
            else s.mkString
        }
      ),
      // todo: https://gohugo.io/functions/jsonify/
      TemplateFunction("length", 1, {
        case (con, Seq(s: String))      => s.length
        case (con, Seq(s: Iterable[_])) => s.size
      }),
      TemplateFunction("lower", 1, { case (con, Seq(s: String)) => s.toLowerCase }),
      TemplateFunction("map", 2, {
        case (con, Seq(NonStrictExpr(expr), s: Iterable[_])) => s map (e => con.copy(data = e).eval(expr))
        case (con, Seq(s: String))                           => s // todo: map named function
      }),
      // todo: https://gohugo.io/functions/markdownify/
      TemplateFunction("max", 1, { case (con, Seq(a: BigDecimal, b: BigDecimal)) => a max b }),
      // todo: https://gohugo.io/functions/md5/
      // todo: https://gohugo.io/functions/merge/
      TemplateFunction("min", 1, { case (con, Seq(a: BigDecimal, b: BigDecimal)) => a min b }),
      TemplateFunction("now", 0, _ => Datetime.now().timestamp),
      TemplateFunction("number", 1, { case (con, Seq(s: String)) => BigDecimal(s) }),
      TemplateFunction(
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
      TemplateFunction("partial", 1, {
        case (con, Seq(path: String))            => partial(con, path, null)
        case (con, Seq(path: String, data: Any)) => partial(con, path, data)
      }),
      // todo: https://gohugo.io/functions/querify/
      // todo: https://gohugo.io/functions/readdir/
      // todo: https://gohugo.io/functions/replace/
      // todo: https://gohugo.io/functions/replaceRE/
      TemplateFunction("reverse", 1, { case (con, Seq(s: Seq[_])) => s.reverse }),
      // todo: https://gohugo.io/functions/sha/
      TemplateFunction("shuffle", 1, { case (con, Seq(s: Seq[_])) => Random.shuffle(s) }),
      // todo: https://gohugo.io/functions/singularize/
      // todo: https://gohugo.io/functions/sort/
      TemplateFunction("split", 2, { case (con, Seq(delim: String, s: String)) => s split delim toSeq }),
      TemplateFunction("substring", 3, {
        case (con, Seq(start: BigDecimal, end: BigDecimal, s: String)) => s.substring(start.toIntExact, end.toIntExact)
      }),
      TemplateFunction("startsWith", 2, { case (con, Seq(prefix: String, s: String)) => s startsWith prefix }),
      // todo: https://gohugo.io/functions/strings.count/
      TemplateFunction("take", 2, {
        case (con, Seq(n: Num, s: Iterable[_])) => s take n.toIntExact
        case (con, Seq(n: Num, s: String))      => s take n.toIntExact
      }),
      // todo: https://gohugo.io/functions/symdiff/
      TemplateFunction("takeRight", 2, {
        case (con, Seq(n: Num, s: Iterable[_])) => s takeRight n.toIntExact
        case (con, Seq(n: Num, s: String))      => s takeRight n.toIntExact
      }),
      TemplateFunction("time", 1, { case (con, Seq(s: String)) => Datetime.fromString(s) }),
      // todo: https://gohugo.io/functions/title/
      TemplateFunction("toSeq", 1, { case (con, Seq(s: Iterable[_])) => s.toSeq }),
      TemplateFunction("toString", 1, { case (con, Seq(a: Any))      => a.toString }),
      TemplateFunction("trim", 1, { case (con, Seq(s: String))       => s.trim }), // todo: https://gohugo.io/functions/trim/
      // todo: https://gohugo.io/functions/truncate/
      // todo: https://gohugo.io/functions/union/
      TemplateFunction("unix", 1, { case (con, Seq(d: Datetime)) => BigDecimal(d.epochMillis) }),
      // todo: https://gohugo.io/functions/transform.unmarshal/
      TemplateFunction("upper", 1, { case (con, Seq(s: String)) => s.toUpperCase }),
      // todo: https://gohugo.io/functions/urlize/
    ) map (f => (f.name, f)) toMap

  private def findRE(re: String, s: String) =
    re.r.findAllMatchIn(s) map { m =>
      val res = new ListBuffer[String]

      res += m.matched

      for (i <- 1 to m.groupCount)
        res += m.group(i)

      res.toList
    }

  private def partial(context: Context, path: String, data: Any): Any = {
    val partial = context.renderer.partials(path) getOrElse sys.error(s"partial '$path' count not be loaded")

    context.renderer.render(data, partial, context.out)
  }

}
