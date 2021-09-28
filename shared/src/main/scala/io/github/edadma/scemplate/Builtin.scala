package io.github.edadma.scemplate

import io.github.edadma.datetime.Datetime

import scala.language.postfixOps

object Builtin {

  val functions: Map[String, BuiltinFunction] =
    List(
      BuiltinFunction("anchorize", 1, {
        case Seq(s: String) =>
      }),
      BuiltinFunction("append", 2, {
        case Seq(c: Seq[_], s: Seq[_])                => s ++ c
        case s: Seq[_] if s.last.isInstanceOf[Seq[_]] => s.last.asInstanceOf[Seq[_]] ++ s.init
      }),
      BuiltinFunction("drop", 2, { case Seq(n: Num, s: Iterable[_]) => s drop n.toIntExact }),
      BuiltinFunction("map", 2, {
        case Seq(NonStrictExpr(expr), s: Iterable[_]) => s map ()
        case Seq(s: String)                           => BigDecimal(s)
      }),
      BuiltinFunction("now", 0, _ => Datetime.now().timestamp),
      BuiltinFunction("number", 1, { case Seq(s: String)            => BigDecimal(s) }),
      BuiltinFunction("take", 2, { case Seq(n: Num, s: Iterable[_]) => s take n.toIntExact }),
      BuiltinFunction("unix", 1, { case Seq(d: Datetime)            => BigDecimal(d.epochMillis) }),
    ) map (f => (f.name, f)) toMap

}
