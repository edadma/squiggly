package io.github.edadma.scemplate

import io.github.edadma.datetime.Datetime

import scala.language.postfixOps

object Builtin {

  val functions: Map[String, BuiltinFunction] =
    List(
      BuiltinFunction("anchorize", Set(1), {
        case Seq(s: String) =>
      }),
      BuiltinFunction("append", Set(), {
        case s: Seq[_] if s.last.isInstanceOf[Seq[_]] =>
      }),
      BuiltinFunction("drop", Set(2), { case Seq(n: Num, s: Iterable[_]) => s drop n.toIntExact }),
      BuiltinFunction("now", Set(0), _ => Datetime.now().timestamp),
      BuiltinFunction("take", Set(2), { case Seq(n: Num, s: Iterable[_]) => s take n.toIntExact }),
      BuiltinFunction("unix", Set(1), { case Seq(d: Datetime)            => BigDecimal(d.epochMillis) }),
    ) map (f => (f.name, f)) toMap

}
