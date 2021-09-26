package io.github.edadma.scemplate

import io.github.edadma.yaml._

object platformSpecific extends Platform {

  def yaml(s: String): Any = {
    def construct(n: YamlNode): Any =
      n match {
        case MapYamlNode(entries) => entries map { case (k, v) => (construct(k), construct(v)) } toMap
        case IntYamlNode(n)       => BigDecimal(n)
        case FloatYamlNode(n)     => BigDecimal(n)
        case StringYamlNode(s)    => s
        case BooleanYamlNode(b)   => b == "true"
        case SeqYamlNode(elems)   => elems map construct
      }

    construct(readFromString(s))
  }

}