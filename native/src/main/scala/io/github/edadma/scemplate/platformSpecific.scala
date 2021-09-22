package io.github.edadma.scemplate

import io.github.edadma.libyaml.constructFromString

object platformSpecific extends Platform {

  def yaml(s: String): Any = constructFromString(s).headOption.orNull

}
