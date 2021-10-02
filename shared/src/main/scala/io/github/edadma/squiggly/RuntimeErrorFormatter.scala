package io.github.edadma.squiggly

import org.parboiled2.{ErrorFormatter, ParseError, ParserInput}

class RuntimeErrorFormatter(msg: String) extends ErrorFormatter {

  import java.lang.{StringBuilder => JStringBuilder}

  def customFormat(error: ParseError, input: ParserInput): String = {
    import error._

    val sb = new JStringBuilder(128)

    sb.append(msg)
    sb.append(" (line ").append(position.line).append(", column ").append(position.column).append(')')
    formatErrorLine(sb.append(':').append('\n'), error, input).toString
  }

}
