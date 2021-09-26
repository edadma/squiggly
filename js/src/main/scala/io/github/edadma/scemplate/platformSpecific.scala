package io.github.edadma.scemplate

import io.github.edadma.yaml._

object platformSpecific extends Platform {

  def yaml(s: String): Any = {
    def construct(n: YamlNode): Any =
      n match {
        case MapYamlNode(entries) => entries map { case (k, v) => (construct(k), construct(v)) } toMap
        case IntYamlNode(n)       => n.toInt
        case StringYamlNode(s)    => s
        case SeqYamlNode(elems)   => elems map construct
      }

    construct(readFromString(s))
  }

}
