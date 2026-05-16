package io.github.edadma.squiggly

/** URL slug helpers — ASCII folding of Latin diacritics, then
  * normalization to a `[a-z0-9-]+` token suitable for paths, fragment
  * IDs, file stems, etc.
  *
  * Lives in `squiggly` (not in the SSG framework on top of it) because
  * it's a pure value transform with no I/O and no site-config
  * dependency — exactly the shape a template engine builtin wants.
  * Exposed as a public Scala function so non-template callers (e.g.
  * an SSG building taxonomy archive paths) don't have to round-trip
  * through the template renderer to slugify a string.
  */
object Slug {

  /** ASCII-fold mapping for common Western European diacritics. Each
    * entry is `<combined char> -> <replacement>`. Characters absent
    * from the table pass through untouched — [[slugify]] then decides
    * what to do with them (typically: drop, since they're not in
    * `[a-z0-9]`). Output strings can be longer than one char (e.g.
    * `ß` → `ss`) so callers must use a `StringBuilder`, not a 1:1
    * character map. */
  val diacriticFold: Map[Char, String] = Map(
    'à' -> "a", 'á' -> "a", 'â' -> "a", 'ã' -> "a", 'ä' -> "a", 'å' -> "a", 'ā' -> "a", 'ă' -> "a", 'ą' -> "a",
    'ç' -> "c", 'ć' -> "c", 'č' -> "c",
    'ď' -> "d",
    'è' -> "e", 'é' -> "e", 'ê' -> "e", 'ë' -> "e", 'ē' -> "e", 'ė' -> "e", 'ę' -> "e", 'ě' -> "e",
    'ğ' -> "g",
    'ì' -> "i", 'í' -> "i", 'î' -> "i", 'ï' -> "i", 'ī' -> "i", 'į' -> "i", 'ı' -> "i",
    'ł' -> "l", 'ľ' -> "l",
    'ñ' -> "n", 'ń' -> "n", 'ň' -> "n",
    'ò' -> "o", 'ó' -> "o", 'ô' -> "o", 'õ' -> "o", 'ö' -> "o", 'ø' -> "o", 'ō' -> "o", 'ő' -> "o",
    'ŕ' -> "r", 'ř' -> "r",
    'ś' -> "s", 'š' -> "s", 'ş' -> "s",
    'ť' -> "t", 'ţ' -> "t",
    'ù' -> "u", 'ú' -> "u", 'û' -> "u", 'ü' -> "u", 'ū' -> "u", 'ů' -> "u", 'ű' -> "u",
    'ý' -> "y", 'ÿ' -> "y",
    'ź' -> "z", 'ż' -> "z", 'ž' -> "z",
    'ß' -> "ss", 'æ' -> "ae", 'œ' -> "oe", 'ð' -> "d", 'þ' -> "th",
  )

  /** ASCII-fold a string by mapping known Latin diacritics. Characters
    * not in the table pass through unchanged; the [[slugify]] step
    * then decides what to do with them. */
  def asciiFold(s: String): String = {
    val sb = new StringBuilder(s.length)
    var i  = 0
    while (i < s.length) {
      val c = s.charAt(i)
      diacriticFold.get(c) match {
        case Some(repl) => sb.append(repl)
        case None       => sb.append(c)
      }
      i += 1
    }
    sb.toString
  }

  /** Convert a free-form string into a URL-safe slug. Lowercases,
    * ASCII-folds common Latin diacritics, replaces every run of
    * non-alphanumeric ASCII with a single `-`, and trims leading and
    * trailing dashes. Empty / all-symbol inputs collapse to `"-"` so
    * the result is always a non-empty path segment.
    *
    * Examples:
    * {{{
    *   slugify("Hello, World!")  == "hello-world"
    *   slugify("Café au lait")   == "cafe-au-lait"
    *   slugify("C++")            == "c"
    *   slugify("___")            == "-"
    * }}}
    *
    * Squiggly also exposes this as a `slugify` template builtin (see
    * [[TemplateBuiltin]]); the Scala-callable form is provided so
    * frameworks built on squiggly (e.g. juicer) can compute taxonomy
    * archive paths from frontmatter without round-tripping through
    * the renderer.
    */
  def slugify(s: String): String = {
    val folded   = asciiFold(s.toLowerCase)
    val sb       = new StringBuilder(folded.length)
    var lastDash = false
    var i        = 0
    while (i < folded.length) {
      val c = folded.charAt(i)
      if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) {
        sb.append(c)
        lastDash = false
      } else if (!lastDash && sb.nonEmpty) {
        sb.append('-')
        lastDash = true
      }
      i += 1
    }
    // Trim a trailing dash that the loop may have left behind. Leading
    // dashes can't happen because dashes are suppressed until at
    // least one alnum has landed.
    val out = if (sb.nonEmpty && sb.last == '-') sb.dropRight(1).toString else sb.toString
    if (out.isEmpty) "-" else out
  }
}
