package io.github.edadma.scemplate

import io.github.edadma.datetime.Datetime

import scala.language.postfixOps

object Builtin {

  val functions: Map[String, BuiltinFunction] =
    List(
      BuiltinFunction("now", Set(0), _ => Datetime.now().timestamp),
      BuiltinFunction("unix", Set(1), { case Seq(d: Datetime)              => BigDecimal(d.epochMillis) }),
      BuiltinFunction("take", Set(2), { case Seq(n: BigDecimal, s: Seq[_]) => s take n.toIntExact })
    ) map (f => (f.name, f)) toMap

}
