package io.github.edadma.squiggly

import io.github.edadma.commonmark

import java.math.{MathContext, RoundingMode}
import io.github.edadma.cross_platform._
import io.github.edadma.datetime.{Datetime, DatetimeFormatter}

import java.nio.file.Paths
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.language.postfixOps
import scala.util.Random
import scala.util.matching.Regex

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
      TemplateFunction("absURL", 1, {
        case (con, Seq(arg: String)) =>
          val BaseURL(base, path) = con.renderer.data("baseURL").asInstanceOf[BaseURL]

          s"$base${Paths.get(path) resolve arg}"
      }),
      TemplateFunction("append", 2, { case (con, Seq(e: Any, s: Seq[_])) => s :+ e }),
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
      TemplateFunction(
        "complement",
        1, {
          case (con, cs: Seq[_]) =>
            require(cs forall (_.isInstanceOf[Seq[Any]]))

            val union = mutable.LinkedHashSet.concat[Any](cs.asInstanceOf[Seq[Seq[Any]]].init: _*)

            cs.last.asInstanceOf[Seq[Any]] to mutable.LinkedHashSet diff union toList
        }
      ),
      // todo: https://gohugo.io/functions/complement/
      TemplateFunction(
        "contains",
        2, {
          case (con, Seq(elem: Any, s: Seq[_]))       => s contains elem
          case (con, Seq(elem: String, m: Map[_, _])) => m.asInstanceOf[Map[String, _]] contains elem
          case (con, Seq(substr: String, s: String))  => s contains substr
        }
      ),
      TemplateFunction("context", 0, { case (con, _) => println(con) }),
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
      TemplateFunction("floor", 1, {
        case (con, Seq(n: BigDecimal)) => n.round(new MathContext(n.mc.getPrecision, RoundingMode.FLOOR))
      }),
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
      TemplateFunction("head", 1, { case (con, Seq(s: Seq[_])) => s.head }),
      // todo: https://gohugo.io/functions/highlight/
      // todo: https://gohugo.io/functions/hmac/
      TemplateFunction("htmlEscape", 1, {
        case (con, Seq(s: String)) =>
          s replace ("&", "&amp;") replace ("<", "&lt;") replace (">", "&gt;") replace ("'", "&apos;") replace ("\"", "&quot;")
      }),
      // todo: https://shopify.github.io/liquid/filters/escape_once/
      // todo: htmlUnescape
      // todo: https://gohugo.io/functions/humanize/
      // todo: https://gohugo.io/functions/i18n/
      TemplateFunction("intersect", 2, {
        case (con, Seq(s1: Seq[Any], s2: Seq[Any])) =>
          (s1 to mutable.LinkedHashSet) intersect (s2 to mutable.LinkedHashSet) toList
      }),
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
      TemplateFunction("last", 1, { case (con, Seq(s: Seq[_])) => s.last }),
      TemplateFunction("length", 1, {
        case (con, Seq(s: String))      => s.length
        case (con, Seq(s: Iterable[_])) => s.size
      }),
      TemplateFunction("lower", 1, { case (con, Seq(s: String)) => s.toLowerCase }),
      TemplateFunction(
        "map",
        2, {
          case (con, Seq(NonStrictExpr(expr), s: Iterable[_])) => s map (e => con.copy(data = e).eval(expr))
          //case (con, Seq(s: String))                           => s // todo: map named function
        }
      ),
      TemplateFunction(
        "markdownify",
        1, {
          case (con, Seq(s: String)) =>
            commonmark.Util
              .html(markdownParser.parse(s), 2, link = con.renderer.data("link").asInstanceOf[String => String])
              .trim
        }
      ),
      TemplateFunction("max", 1, { case (con, Seq(a: BigDecimal, b: BigDecimal)) => a max b }),
      // todo: https://gohugo.io/functions/merge/
      TemplateFunction("min", 1, { case (con, Seq(a: BigDecimal, b: BigDecimal)) => a min b }),
      TemplateFunction("now", 0, _ => Datetime.now().timestamp),
      TemplateFunction("newline_to_br", 1, { case (con, Seq(s: String)) => s.replace("\n", "<br />\n") }),
      TemplateFunction("number", 1, { case (con, Seq(s: String))        => BigDecimal(s) }),
      // todo: https://gohugo.io/functions/path.base/
      // todo: https://gohugo.io/functions/pluralize/
      TemplateFunction("partial", 1, {
        case (con, Seq(path: String))            => partial(con, path, null)
        case (con, Seq(path: String, data: Any)) => partial(con, path, data)
      }),
      TemplateFunction("prepend", 2, { case (con, Seq(e: Any, s: Seq[_])) => e +: s }),
      TemplateFunction("print", 0, { case (con, args)                     => print(args mkString ", ") }),
      TemplateFunction("println", 0, { case (con, args)                   => println(args mkString ", ") }),
      TemplateFunction("querify", 1, {
        case (con, Seq(m: collection.Map[_, _])) => m map { case (k, v) => s"$k=$v" } mkString "&"
      }),
      // todo: https://gohugo.io/functions/querify/
      // todo: https://gohugo.io/functions/readdir/
      TemplateFunction("random", 1, { case (con, Seq(s: Seq[_])) => s(Random.nextInt(s.length)) }),
      TemplateFunction("relURL", 1, {
        case (con, Seq(arg: String)) =>
          Paths.get(con.renderer.data("baseURL").asInstanceOf[BaseURL].path) resolve arg toString
      }),
      // todo: https://gohugo.io/functions/replace/
      // todo: https://gohugo.io/functions/replaceRE/
      TemplateFunction("reverse", 1, {
        case (con, Seq(s: Seq[_])) => s.reverse
        case (con, Seq(s: String)) => s.reverse
      }),
      TemplateFunction("remove", 1, { case (con, Seq(item: String, s: String)) => s.replace(item, "") }),
      TemplateFunction("removeFirst", 1, {
        case (con, Seq(item: String, s: String)) => s.replaceFirst(Regex.quote(item), "")
      }),
      TemplateFunction("round", 1, {
        case (con, Seq(n: BigDecimal)) => n.round(new MathContext(n.mc.getPrecision, RoundingMode.HALF_EVEN))
      }),
      // todo: https://gohugo.io/functions/sha/
      TemplateFunction("shuffle", 1, { case (con, Seq(s: Seq[_])) => Random.shuffle(s) }),
      // todo: https://gohugo.io/functions/singularize/
      TemplateFunction(
        "slice",
        2, {
          case (con, Seq(from: Num, s: Iterable[_]))             => s slice (from.toIntExact, s.size)
          case (con, Seq(from: Num, until: Num, s: Iterable[_])) => s slice (from.toIntExact, until.toIntExact)
          case (con, Seq(from: Num, s: String))                  => s slice (from.toIntExact, s.length)
          case (con, Seq(from: Num, until: Num, s: String))      => s slice (from.toIntExact, until.toIntExact)
        }
      ),
      //      TemplateFunction("sort", 1, {// todo: sortNatural (case-insensitive)
      //        case (con, Seq(s: Seq[_]))                      =>
      //        case (con, Seq(NonStrictExpr(expr), s: Seq[_])) =>
      //      }),
      TemplateFunction("split", 2, { case (con, Seq(delim: String, s: String))       => s split Regex.quote(delim) toSeq }),
      TemplateFunction("startsWith", 2, { case (con, Seq(prefix: String, s: String)) => s startsWith prefix }),
      TemplateFunction("substring", 3, {
        case (con, Seq(start: BigDecimal, end: BigDecimal, s: String)) => s.substring(start.toIntExact, end.toIntExact)
      }),
      TemplateFunction("sum", 1, { case (con, Seq(s: Seq[_])) => s.asInstanceOf[Seq[BigDecimal]].sum }),
      // todo: https://gohugo.io/functions/strings.count/
      TemplateFunction("tail", 1, { case (con, Seq(s: Seq[_])) => s.tail }),
      TemplateFunction("take", 2, {
        case (con, Seq(n: Num, s: Iterable[_])) => s take n.toIntExact
        case (con, Seq(n: Num, s: String))      => s take n.toIntExact
      }),
      TemplateFunction(
        "symdiff",
        2, {
          case (con, Seq(s1: Seq[Any], s2: Seq[Any])) =>
            val c1 = s1 to mutable.LinkedHashSet
            val c2 = s2 to mutable.LinkedHashSet

            (c1 union c2) diff (c1 intersect c2) toList
        }
      ),
      TemplateFunction("takeRight", 2, {
        case (con, Seq(n: Num, s: Iterable[_])) => s takeRight n.toIntExact
        case (con, Seq(n: Num, s: String))      => s takeRight n.toIntExact
      }),
      TemplateFunction("time", 1, { case (con, Seq(s: String)) => Datetime.fromString(s) }),
      // todo: https://gohugo.io/functions/title/ https://en.wikipedia.org/wiki/Title_case
      TemplateFunction("toSeq", 1, { case (con, Seq(s: Iterable[_])) => s.toSeq }),
      TemplateFunction("toString", 1, { case (con, Seq(a: Any))      => a.toString }),
      TemplateFunction("trim", 1, { case (con, Seq(s: String))       => s.trim }), // todo: https://gohugo.io/functions/trim/
      TemplateFunction(
        "truncate",
        1, {
          case (con, Seq(n: BigDecimal, s: String))                   => truncate(s, n.toIntExact, "...")
          case (con, Seq(n: BigDecimal, s: String, ellipsis: String)) => truncate(s, n.toIntExact, ellipsis)
        }
      ),
      TemplateFunction("ltrim", 1, { case (con, Seq(s: String)) => s dropWhile (_.isWhitespace) }), // todo: https://gohugo.io/functions/trim/
      TemplateFunction("rtrim", 1, { case (con, Seq(s: String)) => s.reverse dropWhile (_.isWhitespace) reverse }), // todo: https://gohugo.io/functions/trim/
      TemplateFunction(
        "urlDecode",
        1, {
          case (con, Seq(s: String)) =>
            var i = 0
            val buf = new StringBuilder

            while (i < s.length) {
              s(i) match {
                case '%' =>
                  i += 1

                  if (i > s.length - 2)
                    sys.error(s"2 digit code expected at column $i: $s")

                  try {
                    buf += Integer.parseInt(s.substring(i, i + 2), 16).toChar
                  } catch {
                    case e: NumberFormatException => sys.error(s"error decoding URL at column $i: $s")
                  }

                  i += 2
                case '+' =>
                  buf += ' '
                  i += 1
                case _ =>
                  buf += s(i)
                  i += 1
              }
            }

            buf.toString
        }
      ),
      TemplateFunction(
        "urlEncode",
        1, {
          case (con, Seq(s: String)) =>
            s flatMap {
              case ' '  => "+"
              case '!'  => "%21"
              case '#'  => "%23"
              case '$'  => "%24"
              case '%'  => "%25"
              case '&'  => "%26"
              case '\'' => "%27"
              case '('  => "%28"
              case ')'  => "%29"
              case '*'  => "%2A"
              case '+'  => "%2B"
              case ','  => "%2C"
              case '/'  => "%2F"
              case ':'  => "%3A"
              case ';'  => "%3B"
              case '='  => "%3D"
              case '?'  => "%3F"
              case '@'  => "%40"
              case '['  => "%5B"
              case ']'  => "%5D"
              case c    => c.toString
            }
        }
      ),
      TemplateFunction("union", 2, {
        case (con, Seq(s1: Seq[Any], s2: Seq[Any])) =>
          (s1 to mutable.LinkedHashSet) union (s2 to mutable.LinkedHashSet) toList
      }),
      TemplateFunction("unix", 1, { case (con, Seq(d: Datetime)) => BigDecimal(d.epochMillis) }),
      // todo: https://gohugo.io/functions/transform.unmarshal/
      TemplateFunction("upper", 1, { case (con, Seq(s: String)) => s.toUpperCase }),
      TemplateFunction(
        "urlize",
        1, {
          case (con, Seq(s: String)) =>
            val buf = new StringBuilder(s.trim)

            if (buf.isEmpty) "-"
            else {
              var i = 0

              while (i < buf.length) {
                if (buf(i) == ' ') {
                  if (i > 0 && buf(i - 1) == '-')
                    buf.deleteCharAt(i)
                  else {
                    buf(i) = '-'
                    i += 1
                  }
                } else if (buf(i).isLetterOrDigit)
                  i += 1
                else
                  buf.deleteCharAt(i)
              }

              buf.toString
            }
        }
      ),
    ) map (f => (f.name, f)) toMap

  private val WS_REGEX = "\\s+".r

  private def truncate(s: String, n: Int, ellipsis: String): String = {
    val it = WS_REGEX.findAllMatchIn(s).drop(n - 1)

    if (it.hasNext) s"${s.substring(0, it.next().start)}$ellipsis"
    else s
  }

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
