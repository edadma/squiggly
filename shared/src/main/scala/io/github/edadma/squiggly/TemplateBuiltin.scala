package io.github.edadma.squiggly

import java.time.{Instant, LocalDate, OffsetDateTime, ZoneOffset}
import java.time.format.{DateTimeFormatter, DateTimeParseException}
import java.util.Locale
import io.github.edadma.cross_platform._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.language.postfixOps
import scala.util.Random
import scala.util.matching.Regex

// `absURL` / `relURL` still live in the SSG layer (juicer) because they
// need a baseURL from site config — squiggly has no notion of a site.
// Date / time builtins (now / time / unix / format) live below on java.time;
// `scala-java-time` 2.6.0 supplies that on JS and Native.

object TemplateBuiltin {

  // Date formatting locale. Pinned to en-US explicitly because the default
  // locale on Scala Native (and on Scala.js without configuration) is ROOT,
  // which renders MMM/MMMM/EEEE as numeric stubs ("M03" instead of "Mar")
  // because CLDR data isn't available for the empty locale tag.
  private val DATE_LOCALE = Locale.US

  // Named date formats used by `format ':date_*' <date>`. Patterns are
  // explicit (rather than DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL))
  // so output is identical across JVM, JS, and Native regardless of the
  // ambient locale.
  private val DATE_FULL_FORMAT   = DateTimeFormatter.ofPattern("EEEE, MMMM d, uuuu", DATE_LOCALE)
  private val DATE_LONG_FORMAT   = DateTimeFormatter.ofPattern("MMMM d, uuuu",       DATE_LOCALE)
  private val DATE_MEDIUM_FORMAT = DateTimeFormatter.ofPattern("MMM d, uuuu",        DATE_LOCALE)
  private val DATE_SHORT_FORMAT  = DateTimeFormatter.ofPattern("M/d/uu",             DATE_LOCALE)

  /** Parse a string as one of: OffsetDateTime ("...Z" / "...+HH:MM" — preferred),
    * Instant, or LocalDate. Throws if none of those match.
    */
  private def parseDateTime(s: String): Any =
    try OffsetDateTime.parse(s)
    catch
      case _: DateTimeParseException =>
        try Instant.parse(s)
        catch
          case _: DateTimeParseException =>
            try LocalDate.parse(s)
            catch case _: DateTimeParseException => sys.error(s"not a date/time: $s")

  /** Convert any supported date/time-ish value to epoch milliseconds. */
  private def epochMillis(v: Any): Long = v match
    case d: OffsetDateTime => d.toInstant.toEpochMilli
    case i: Instant        => i.toEpochMilli
    case d: LocalDate      => d.atStartOfDay(ZoneOffset.UTC).toInstant.toEpochMilli
    case s: String         => epochMillis(parseDateTime(s))
    case other             => sys.error(s"not a date/time: $other")

  /** Format any supported date/time-ish value with the given pattern. */
  private def formatDate(pattern: String, v: Any): String =
    val fmt = DateTimeFormatter.ofPattern(pattern, DATE_LOCALE)
    v match
      case d: OffsetDateTime => fmt.format(d)
      case i: Instant        => fmt.format(i.atOffset(ZoneOffset.UTC))
      case d: LocalDate      => fmt.format(d)
      case s: String         => formatDate(pattern, parseDateTime(s))
      case other             => sys.error(s"not a date/time: $other")

  private def formatNamed(fmt: DateTimeFormatter, v: Any): String = v match
    case d: OffsetDateTime => fmt.format(d)
    case i: Instant        => fmt.format(i.atOffset(ZoneOffset.UTC))
    case d: LocalDate      => fmt.format(d)
    case s: String         => formatNamed(fmt, parseDateTime(s))
    case other             => sys.error(s"not a date/time: $other")

  val namespaces: Map[String, Map[String, TemplateFunction]] =
    Map(
      "images" -> Map(),
      "lang" -> Map(),
      "math" -> Map(),
      "path" -> Map(),
      "strings" -> Map(),
    )
  val functions: Map[String, TemplateFunction] =
    List(
      TemplateFunction("abs", 1, { case (con, Seq(n: BigDecimal)) => n.abs }),
      //      BuiltinFunction("anchorize", 1, {
      //        case (con, Seq(s: String)) =>
      //      }),
      // TODO: absURL — needs a cross-platform path joiner (java.nio.file.Paths is JVM only).
      TemplateFunction("append", 2, { case (con, Seq(e: Any, s: Seq[?])) => s :+ e }),
      // todo: https://gohugo.io/functions/base64/
      TemplateFunction(
        "capitalize",
        1,
        { case (con, Seq(s: String)) =>
          if (s.nonEmpty) s.head.toUpper +: s.tail.toLowerCase
          else s
        },
      ),
      TemplateFunction(
        "ceil",
        1,
        { case (con, Seq(n: BigDecimal)) => n.setScale(0, BigDecimal.RoundingMode.CEILING) },
      ),
      TemplateFunction("compact", 1, { case (con, Seq(s: Seq[?])) => s.filterNot(e => e == () || e == null) }),
      TemplateFunction(
        "complement",
        1,
        { case (con, cs: Seq[?]) =>
          require(cs forall (_.isInstanceOf[Seq[Any]]))

          val union = mutable.LinkedHashSet.concat[Any](cs.asInstanceOf[Seq[Seq[Any]]].init*)

          cs.last.asInstanceOf[Seq[Any]] to mutable.LinkedHashSet diff union toList
        },
      ),
      // todo: https://gohugo.io/functions/complement/
      TemplateFunction(
        "contains",
        2,
        {
          case (con, Seq(elem: Any, s: Seq[?]))       => s.contains(elem)
          case (con, Seq(elem: String, m: Map[?, ?])) => m.asInstanceOf[Map[String, ?]].contains(elem)
          case (con, Seq(substr: String, s: String))  => s.contains(substr)
        },
      ),
      TemplateFunction("context", 0, { case (con, _) => println(con) }),
      TemplateFunction(
        "default",
        2,
        {
          case (con, Seq(default: Any, () | "" | `ZERO`))   => default
          case (con, Seq(default: Any, input: Iterable[?])) => if (input.nonEmpty) input else default
          case (con, Seq(_, input))                         => input
        },
      ),
      TemplateFunction("distinct", 1, { case (con, Seq(s: Seq[?])) => s.distinct }),
      TemplateFunction(
        "drop",
        2,
        {
          case (con, Seq(n: Num, s: Iterable[?])) => s drop n.toIntExact
          case (con, Seq(n: Num, s: String))      => s drop n.toIntExact
        },
      ),
      TemplateFunction(
        "dropRight",
        2,
        {
          case (con, Seq(n: Num, s: Iterable[?])) => s dropRight n.toIntExact
          case (con, Seq(n: Num, s: String))      => s dropRight n.toIntExact
        },
      ),
      // TODO: emojify — was `Emoji(s)` from io.github.edadma.emoji; reintroduce when that lib is cross-built.
      TemplateFunction("fileExists", 1, { case (con, Seq(file: String)) => readableFile(file) }),
      TemplateFunction(
        "filter",
        2,
        { case (con, Seq(NonStrictExpr(expr), s: Iterable[?])) =>
          s filter (e => con.copy(data = e).beval(expr))
        },
      ),
      TemplateFunction(
        "filterNot",
        2,
        { case (con, Seq(NonStrictExpr(expr), s: Iterable[?])) =>
          s filterNot (e => con.copy(data = e).beval(expr))
        },
      ),
      TemplateFunction(
        "findRE",
        2,
        {
          case (con, Seq(pattern: String, input: String)) => findRE(pattern, input).toList
          case (con, Seq(pattern: String, input: String, limit: Num)) =>
            findRE(pattern, input).take(limit.toIntExact).toList
        },
      ),
      TemplateFunction(
        "floor",
        1,
        { case (con, Seq(n: BigDecimal)) => n.setScale(0, BigDecimal.RoundingMode.FLOOR) },
      ),
      TemplateFunction(
        "format",
        2,
        {
          case (con, Seq(":date_full",   v: Any)) => formatNamed(DATE_FULL_FORMAT,   v)
          case (con, Seq(":date_long",   v: Any)) => formatNamed(DATE_LONG_FORMAT,   v)
          case (con, Seq(":date_medium", v: Any)) => formatNamed(DATE_MEDIUM_FORMAT, v)
          case (con, Seq(":date_short",  v: Any)) => formatNamed(DATE_SHORT_FORMAT,  v)
          case (con, Seq(pattern: String, v: Any)) => formatDate(pattern, v)
        },
      ),
      // todo: https://gohugo.io/functions/getenv/
      // todo: https://gohugo.io/functions/group/
      TemplateFunction("head", 1, { case (con, Seq(s: Seq[?])) => s.head }),
      // todo: https://gohugo.io/functions/highlight/
      // todo: https://gohugo.io/functions/hmac/
      TemplateFunction(
        "htmlEscape",
        1,
        { case (con, Seq(s: String)) =>
          s.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("'", "&apos;")
            .replace("\"", "&quot;")
        },
      ),
      // todo: https://shopify.github.io/liquid/filters/escape_once/
      // todo: htmlUnescape
      // todo: https://gohugo.io/functions/humanize/
      // todo: https://gohugo.io/functions/i18n/
      TemplateFunction(
        "intersect",
        2,
        { case (con, Seq(s1: Seq[Any], s2: Seq[Any])) =>
          (s1 to mutable.LinkedHashSet) intersect (s2 to mutable.LinkedHashSet) toList
        },
      ),
      TemplateFunction(
        "isEmpty",
        1,
        {
          case (con, Seq(s: String))      => s.isEmpty
          case (con, Seq(s: Iterable[?])) => s.isEmpty
        },
      ),
      TemplateFunction(
        "join",
        1,
        {
          case (con, Seq(delim: String, s: Seq[?])) => s mkString delim
          case (con, Seq(delim: String, last: String, s: Seq[?])) =>
            if (s.length >= 2) s.init.mkString(delim) ++ last ++ s.last.toString
            else s.mkString
        },
      ),
      // todo: https://gohugo.io/functions/jsonify/
      TemplateFunction("last", 1, { case (con, Seq(s: Seq[?])) => s.last }),
      TemplateFunction(
        "length",
        1,
        {
          case (con, Seq(s: String))      => s.length
          case (con, Seq(s: Iterable[?])) => s.size
        },
      ),
      TemplateFunction("lower", 1, { case (con, Seq(s: String)) => s.toLowerCase }),
      TemplateFunction(
        "map",
        2,
        { case (con, Seq(NonStrictExpr(expr), s: Iterable[?])) =>
          s map (e => con.copy(data = e).eval(expr))
        // case (con, Seq(s: String))                           => s // todo: map named function
        },
      ),
//      TemplateFunction(
//        "markdownify",
//        1,
//        { case (con, Seq(s: String)) =>
//          commonmark.Util
//            .html(markdownParser.parse(s), 2, link = con.renderer.data("link").asInstanceOf[String => String])
//            .trim
//        },
//      ),
      TemplateFunction("max", 1, { case (con, Seq(a: BigDecimal, b: BigDecimal)) => a max b }),
      // todo: https://gohugo.io/functions/merge/
      TemplateFunction("min", 1, { case (con, Seq(a: BigDecimal, b: BigDecimal)) => a min b }),
      TemplateFunction(
        "nonEmpty",
        1,
        {
          case (con, Seq(s: String))      => s.nonEmpty
          case (con, Seq(s: Iterable[?])) => s.nonEmpty
        },
      ),
      TemplateFunction("now", 0, _ => OffsetDateTime.now()),
      TemplateFunction("newline_to_br", 1, { case (con, Seq(s: String)) => s.replace("\n", "<br />\n") }),
      TemplateFunction("number", 1, { case (con, Seq(s: String)) => BigDecimal(s) }),
      // todo: https://gohugo.io/functions/path.base/
      // todo: https://gohugo.io/functions/pluralize/
      TemplateFunction(
        "partial",
        1,
        {
          case (con, Seq(path: String))            => partial(con, path, null)
          case (con, Seq(path: String, data: Any)) => partial(con, path, data)
        },
      ),
      TemplateFunction("prepend", 2, { case (con, Seq(e: Any, s: Seq[?])) => e +: s }),
      TemplateFunction("print", 0, { case (con, args) => print(args mkString ", ") }),
      TemplateFunction("println", 0, { case (con, args) => println(args mkString ", ") }),
      TemplateFunction(
        "querify",
        1,
        { case (con, Seq(m: collection.Map[?, ?])) =>
          m map { case (k, v) => s"$k=$v" } mkString "&"
        },
      ),
      // todo: https://gohugo.io/functions/querify/
      // todo: https://gohugo.io/functions/readdir/
      TemplateFunction("random", 1, { case (con, Seq(s: Seq[?])) => s(Random.nextInt(s.length)) }),
      // TODO: relURL — needs a cross-platform path joiner.
      // todo: https://gohugo.io/functions/replace/
      // todo: https://gohugo.io/functions/replaceRE/
      TemplateFunction(
        "reverse",
        1,
        {
          case (con, Seq(s: Seq[?])) => s.reverse
          case (con, Seq(s: String)) => s.reverse
        },
      ),
      TemplateFunction("remove", 1, { case (con, Seq(item: String, s: String)) => s.replace(item, "") }),
      TemplateFunction(
        "removeFirst",
        1,
        { case (con, Seq(item: String, s: String)) =>
          s.replaceFirst(Regex.quote(item), "")
        },
      ),
      TemplateFunction(
        "round",
        1,
        { case (con, Seq(n: BigDecimal)) => n.setScale(0, BigDecimal.RoundingMode.HALF_EVEN) },
      ),
      // todo: https://gohugo.io/functions/sha/
      TemplateFunction("shuffle", 1, { case (con, Seq(s: Seq[?])) => Random.shuffle(s) }),
      // todo: https://gohugo.io/functions/singularize/
      TemplateFunction(
        "slice",
        2,
        {
          case (con, Seq(from: Num, s: Iterable[?]))             => s slice (from.toIntExact, s.size)
          case (con, Seq(from: Num, until: Num, s: Iterable[?])) => s slice (from.toIntExact, until.toIntExact)
          case (con, Seq(from: Num, s: String))                  => s slice (from.toIntExact, s.length)
          case (con, Seq(from: Num, until: Num, s: String))      => s slice (from.toIntExact, until.toIntExact)
        },
      ),
      //      TemplateFunction("sort", 1, {// todo: sortNatural (case-insensitive)
      //        case (con, Seq(s: Seq[?]))                      =>
      //        case (con, Seq(NonStrictExpr(expr), s: Seq[?])) =>
      //      }),
      TemplateFunction("split", 2, { case (con, Seq(delim: String, s: String)) => s.split(Regex.quote(delim)).toSeq }),
      TemplateFunction("startsWith", 2, { case (con, Seq(prefix: String, s: String)) => s.startsWith(prefix) }),
      TemplateFunction(
        "substring",
        3,
        { case (con, Seq(start: BigDecimal, end: BigDecimal, s: String)) =>
          s.substring(start.toIntExact, end.toIntExact)
        },
      ),
      TemplateFunction("sum", 1, { case (con, Seq(s: Seq[?])) => s.asInstanceOf[Seq[BigDecimal]].sum }),
      // todo: https://gohugo.io/functions/strings.count/
      TemplateFunction("tail", 1, { case (con, Seq(s: Seq[?])) => s.tail }),
      TemplateFunction(
        "take",
        2,
        {
          case (con, Seq(n: Num, s: Iterable[?])) => s take n.toIntExact
          case (con, Seq(n: Num, s: String))      => s take n.toIntExact
        },
      ),
      TemplateFunction(
        "symdiff",
        2,
        { case (con, Seq(s1: Seq[Any], s2: Seq[Any])) =>
          val c1 = s1 to mutable.LinkedHashSet
          val c2 = s2 to mutable.LinkedHashSet

          (c1 union c2) diff (c1 intersect c2) toList
        },
      ),
      TemplateFunction(
        "takeRight",
        2,
        {
          case (con, Seq(n: Num, s: Iterable[?])) => s takeRight n.toIntExact
          case (con, Seq(n: Num, s: String))      => s takeRight n.toIntExact
        },
      ),
      TemplateFunction("time", 1, { case (con, Seq(s: String)) => parseDateTime(s) }),
      // todo: https://gohugo.io/functions/title/ https://en.wikipedia.org/wiki/Title_case
      TemplateFunction("toSeq", 1, { case (con, Seq(s: Iterable[?])) => s.toSeq }),
      TemplateFunction("toString", 1, { case (con, Seq(a: Any)) => a.toString }),
      TemplateFunction("trim", 1, { case (con, Seq(s: String)) => s.trim }), // todo: https://gohugo.io/functions/trim/
      TemplateFunction(
        "truncate",
        1,
        {
          case (con, Seq(n: BigDecimal, s: String))                   => truncate(s, n.toIntExact, "...")
          case (con, Seq(n: BigDecimal, s: String, ellipsis: String)) => truncate(s, n.toIntExact, ellipsis)
        },
      ),
      TemplateFunction(
        "ltrim",
        1,
        { case (con, Seq(s: String)) => s dropWhile (_.isWhitespace) },
      ), // todo: https://gohugo.io/functions/trim/
      TemplateFunction(
        "rtrim",
        1,
        { case (con, Seq(s: String)) => s.reverse dropWhile (_.isWhitespace) reverse },
      ), // todo: https://gohugo.io/functions/trim/
      TemplateFunction(
        "urlDecode",
        1,
        { case (con, Seq(s: String)) =>
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
        },
      ),
      TemplateFunction(
        "urlEncode",
        1,
        { case (con, Seq(s: String)) =>
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
        },
      ),
      TemplateFunction(
        "union",
        2,
        { case (con, Seq(s1: Seq[Any], s2: Seq[Any])) =>
          (s1 to mutable.LinkedHashSet) union (s2 to mutable.LinkedHashSet) toList
        },
      ),
      TemplateFunction("unix", 1, { case (con, Seq(v: Any)) => BigDecimal(epochMillis(v)) }),
      // todo: https://gohugo.io/functions/transform.unmarshal/
      TemplateFunction("upper", 1, { case (con, Seq(s: String)) => s.toUpperCase }),
      TemplateFunction(
        "urlize",
        1,
        { case (con, Seq(s: String)) =>
          val buf = new StringBuilder(s.trim)

          if (buf.isEmpty) "-"
          else {
            var i = 0

            while (i < buf.length) {
              if (buf(i) == ' ') {
                if (i > 0 && buf(i - 1) == '-') buf.deleteCharAt(i)
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
        },
      ),
      // Substitute `:shortcode:` tokens with the corresponding Unicode
      // emoji via the `io.github.edadma.emoji` library. Unknown
      // shortcodes pass through unchanged.
      TemplateFunction(
        "emojify",
        1,
        { case (con, Seq(s: String)) => io.github.edadma.emoji.Emoji(s) },
      ),
      // JSON-string escape — produces the body of a JSON string
      // (without surrounding quotes). Escapes the characters
      // RFC 8259 §7 requires plus U+2028 / U+2029 (valid JSON but break
      // JavaScript parsers when JSON lands inside an HTML `<script>`).
      // Use for emitting `<script type="application/ld+json">` bodies,
      // attribute values that need JSON encoding, etc.
      TemplateFunction(
        "jsonStr",
        1,
        { case (con, Seq(v: Any)) =>
          val s = v match {
            case null | () => ""
            case x         => x.toString
          }
          val sb = new StringBuilder(s.length + 8)
          var i  = 0
          while (i < s.length) {
            val c = s.charAt(i)
            c match {
              case '"'                    => sb.append("\\\"")
              case '\\'                   => sb.append("\\\\")
              case '\n'                   => sb.append("\\n")
              case '\r'                   => sb.append("\\r")
              case '\t'                   => sb.append("\\t")
              case '\b'                   => sb.append("\\b")
              case '\f'                   => sb.append("\\f")
              case c if c.toInt == 0x2028 => sb.append("\\u2028")
              case c if c.toInt == 0x2029 => sb.append("\\u2029")
              case c if c < 0x20          => sb.append("\\u%04x".format(c.toInt))
              case c                      => sb.append(c)
            }
            i += 1
          }
          sb.toString
        },
      ),
      // Render a markdown string to HTML directly. Uses the markdown
      // library's default config — no syntax highlighter, no per-site
      // extensions. Frameworks that ship a configured markdown engine
      // (e.g. juicer with a `codeHighlighter` plugged in) typically
      // shadow this builtin in their own function map, replacing it
      // with a site-aware version.
      TemplateFunction(
        "markdownify",
        1,
        { case (con, Seq(s: String)) =>
          val cfg = io.github.edadma.markdown.MarkdownConfig()
          io.github.edadma.markdown
            .renderToHTML(io.github.edadma.markdown.parseDocumentContent(s, cfg), cfg)
            .trim
        },
      ),
      // Convert a free-form string into a URL-safe slug — lowercase,
      // ASCII-folded, non-alnum runs collapsed to a single `-`.
      // See [[Slug.slugify]] for the full contract; this just exposes
      // it to templates.
      TemplateFunction(
        "slugify",
        1,
        { case (con, Seq(s: String)) => Slug.slugify(s) },
      ),
      // ---- String filters that round out the existing trim/contains/
      // ---- startsWith family.
      //
      // Replace every occurrence of `from` in `s` with `to`. Useful for
      // converting between separator conventions, fixing up generated
      // text, etc. `from` is matched literally (not as a regex) — for
      // regex replacement use `findRE` + a map pipeline.
      TemplateFunction(
        "replace",
        3,
        { case (con, Seq(from: String, to: String, s: String)) => s.replace(from, to) },
      ),
      // Mirror of `startsWith` for the trailing edge — `endsWith
      // 'suffix' s` is `true` when `s` ends with `suffix`.
      TemplateFunction(
        "endsWith",
        2,
        { case (con, Seq(suffix: String, s: String)) => s.endsWith(suffix) },
      ),
      // ---- List shaping
      //
      // Flatten one level of nesting from a `Seq[Seq[?]]` into a
      // single `Seq[?]`. Useful after a `map` that produces lists
      // (`group → list of pages → flatten → one big page list`).
      // Non-sequence elements pass through wrapped — `[1, [2,3], 4]`
      // flattens to `[1, 2, 3, 4]`.
      TemplateFunction(
        "flatten",
        1,
        { case (con, Seq(s: Seq[?])) =>
          s.flatMap {
            case inner: Seq[?] => inner
            case other         => Seq(other)
          }
        },
      ),
      // ---- Explicit type coercions
      //
      // The existing `number` builtin returns a `BigDecimal` which is
      // squiggly's universal numeric type. These three give templates
      // a way to ask for a *specific* widening: `int` truncates
      // fractions, `float` keeps them as a double-precision value,
      // and `bool` collapses any value to squiggly's truthiness rule
      // (non-empty string, non-zero number, non-empty list/map,
      // non-null, non-false). Accepts strings, BigDecimals, or any
      // value `toString`-able as a number.
      TemplateFunction(
        "int",
        1,
        { case (con, Seq(v: Any)) =>
          val bd: BigDecimal = v match {
            case n: BigDecimal => n
            case s: String     => BigDecimal(s.trim)
            case n: Int        => BigDecimal(n)
            case n: Long       => BigDecimal(n)
            case n: Double     => BigDecimal(n)
            case other         => BigDecimal(other.toString.trim)
          }
          bd.setScale(0, BigDecimal.RoundingMode.DOWN)
        },
      ),
      TemplateFunction(
        "float",
        1,
        { case (con, Seq(v: Any)) =>
          v match {
            case n: BigDecimal => n
            case s: String     => BigDecimal(s.trim)
            case n: Int        => BigDecimal(n)
            case n: Long       => BigDecimal(n)
            case n: Double     => BigDecimal(n)
            case other         => BigDecimal(other.toString.trim)
          }
        },
      ),
      TemplateFunction(
        "bool",
        1,
        { case (con, Seq(v: Any)) =>
          v match {
            case null | ()      => false
            case b: Boolean     => b
            case n: BigDecimal  => n != BigDecimal(0)
            case n: Int         => n != 0
            case n: Long        => n != 0L
            case n: Double      => n != 0.0
            case s: String      => s.nonEmpty
            case s: Seq[?]      => s.nonEmpty
            case m: Map[?, ?]   => m.nonEmpty
            case _              => true
          }
        },
      ),
      // ---- Formatted output (Hugo's `printf`)
      //
      // First arg is a `java.util.Formatter` pattern; remaining args
      // are values. Supports the full Java format-string surface:
      // `%d` / `%05d` / `%,d` (commas), `%f` / `%.2f`, `%s` / `%-10s`
      // (left-pad), `%x` / `%X` / `%o`, `%b`, `%%`. BigDecimals are
      // auto-unwrapped to `Long` or `Double` based on the conversion
      // — `%d` always integer-truncates, `%f` always uses the
      // double-precision value. String args pass through unchanged.
      // Variadic — call with any number of value args after the
      // pattern.
      TemplateFunction(
        "printf",
        0,
        { case (con, args) =>
          args.toList match {
            case (fmt: String) :: rest =>
              val convs    = scanConversions(fmt)
              val javaArgs = rest.zipWithIndex.map { case (v, i) =>
                val conv = if (i < convs.length) convs(i) else ' '
                toJavaArg(conv, v)
              }.toArray
              String.format(fmt, javaArgs*)
            case _ => sys.error("printf: first arg must be a format string")
          }
        },
      ),
    ) map (f => (f.name, f)) toMap

  /** Walk a `printf`-style format string once and collect the
    * conversion character of each active `%` directive in order
    * (`'%%'` literals are skipped). Result length aligns with the
    * argument count the formatter is going to consume — so the
    * caller can zip each user-supplied arg with the conversion the
    * formatter will apply to it, and widen the arg's Java type
    * accordingly. */
  private def scanConversions(fmt: String): IndexedSeq[Char] = {
    val out = scala.collection.mutable.ArrayBuffer.empty[Char]
    var i   = 0
    while (i < fmt.length) {
      if (fmt.charAt(i) == '%' && i + 1 < fmt.length) {
        if (fmt.charAt(i + 1) == '%') { i += 2 }
        else {
          var j = i + 1
          while (j < fmt.length && "0123456789#-+,(. ".contains(fmt.charAt(j))) j += 1
          if (j < fmt.length) {
            out += fmt.charAt(j)
            i = j + 1
          } else i = j
        }
      } else i += 1
    }
    out.toIndexedSeq
  }

  /** Widen one `printf` arg to a Java type the JDK formatter accepts,
    * using `conv` (the format conversion char that will consume it)
    * to choose between `Long` and `Double` for the BigDecimal case.
    * Strings, booleans, and other types pass through as the closest
    * `AnyRef`. */
  private def toJavaArg(conv: Char, v: Any): Object = v match {
    case null            => null
    case ()              => null
    case s: String       => s
    case b: Boolean      => java.lang.Boolean.valueOf(b)
    case n: BigDecimal   =>
      if ("doxXb".contains(conv)) java.lang.Long.valueOf(n.longValue)
      else java.lang.Double.valueOf(n.doubleValue)
    case n: Int          => java.lang.Integer.valueOf(n)
    case n: Long         => java.lang.Long.valueOf(n)
    case n: Double       => java.lang.Double.valueOf(n)
    case n: Float        => java.lang.Float.valueOf(n)
    case other           => other.asInstanceOf[Object]
  }

  private val WS_REGEX = "\\s+".r

  private def truncate(s: String, n: Int, ellipsis: String): String = {
    val it = WS_REGEX.findAllMatchIn(s).drop(n - 1)

    if (it.hasNext) s"${s.substring(0, it.next().start)}$ellipsis"
    else s
  }

  /** Find every match of `re` in `s`. Each entry is a `List[String]` — the
    * full matched text first, then any capture groups (groups that didn't
    * match are returned as empty strings).
    *
    * Templates can index into this in the obvious way:
    *
    * {{{
    *   {{ findRE 'pattern' input }}        // → [[m1, g1, …], [m2, g1, …], …]
    *   {{ (findRE '<p>(.+)</p>' .)[0][1] }}  // → first match's group 1
    * }}}
    *
    * The flat "just the matches" projection a Hugo user might expect can be
    * derived from this with the squiggly pipeline (e.g. `findRE … | map …`).
    */
  private def findRE(re: String, s: String): Iterator[List[String]] =
    re.r.findAllMatchIn(s).map { m =>
      val buf = new ListBuffer[String]
      buf += m.matched
      var i = 1
      while (i <= m.groupCount) {
        buf += Option(m.group(i)).getOrElse("")
        i += 1
      }
      buf.toList
    }

  private def partial(context: Context, path: String, data: Any): Any = {
    val partial = context.renderer.partials(path) getOrElse sys.error(s"partial '$path' count not be loaded")

    context.renderer.render(data, partial, context.out)
  }

}

// todo: implement break/continue https://shopify.github.io/liquid/tags/control-flow/
// todo: implement 'capture' https://shopify.github.io/liquid/tags/variable/
// todo: implement 'unless' https://shopify.github.io/liquid/tags/control-flow/
// todo: implement 'no output' https://shopify.github.io/liquid/tags/template/
// todo: add boolean test for null and undefined
