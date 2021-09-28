package io.github.edadma.scemplate

import io.github.edadma.libyaml.{Constructor, YAMLBigInt, YAMLInteger, YAMLValue, parseFromString}

object platformSpecific extends Platform {

  val bigDecimalConstructor: Constructor =
    new Constructor {
      override def construct(v: YAMLValue): Any = {
        v match {
          case YAMLInteger(n) => BigDecimal(n)
          case YAMLBigInt(n)  => BigDecimal(n)
          case _              => super.construct(v)
        }
      }
    }

  def yaml(s: String): Any = bigDecimalConstructor.construct(parseFromString(s)).headOption.orNull

}
