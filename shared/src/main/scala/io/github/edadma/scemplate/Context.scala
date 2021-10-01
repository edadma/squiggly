package io.github.edadma.scemplate

import scala.annotation.tailrec
import scala.collection.mutable
import scala.language.postfixOps

case class Context(data: Any, functions: Map[String, BuiltinFunction], vars: mutable.HashMap[String, Any]) {

  private var _global: Any = _

  def global_=(d: Any): Unit = {
    require(_global == null)
    _global = d
  }

  def global: Any = {
    require(_global != null)
    _global
  }

  def beval(expr: ExprAST): Boolean = !falsy(eval(expr))

  def neval(expr: ExprAST): BigDecimal =
    eval(expr) match {
      case n: BigDecimal => n
      case s: String     => BigDecimal(s)
      case v             => sys.error(s"not a number: $v")
    }

  // todo: should check arguments for "undefined" (i.e., ())
  def callFunction(pos: TagParser#Position, name: String, args: Seq[Any]): Any =
    functions get name match {
      case Some(BuiltinFunction(_, arity, function)) =>
        if (args.length < arity)
          sys.error(s"too few arguments for function '$name': expected $arity, found ${args.length}")
        else if (!function.isDefinedAt((this, args)))
          sys.error(s"cannot apply function '$name' to arguments ${args map (a => s"'$a'") mkString ", "}")
        else function((this, args))
      case None =>
        if (args.isEmpty) getVar(pos, name)
        else sys.error(s"function found: $name")
    }

  def getVar(pos: TagParser#Position, name: String): Any =
    vars get name match {
      case Some(value) => value
      case None        => sys.error(s"unknown variable: $name")
    }

  def eval(expr: ExprAST): Any =
    expr match {
      case e: NonStrictExpr => e
      case SeqExpr(elems)   => elems map eval
      case MapExpr(pairs)   => pairs map { case (Ident(_, k), v) => (k, eval(v)) } toMap
      case ConditionalAST(cond, yes, no) =>
        if (beval(cond)) eval(yes)
        else if (no.isDefined) eval(no.get)
        else ""
      case CompareExpr(left, right) =>
        var l = eval(left)

        right forall {
          case ("=", expr)  => l == eval(expr)
          case ("!=", expr) => l != eval(expr)
          case (op, expr) =>
            val r = neval(expr)
            val ln = l.asInstanceOf[BigDecimal]

            val res =
              op match {
                case "<"   => ln < r
                case "<="  => ln <= r
                case ">"   => ln > r
                case ">="  => ln >= r
                case "div" => (r remainder ln) == ZERO
              }

            l = r
            res
        }
      case BooleanExpr(_, b) => b
      case StringExpr(_, s)  => s
      case NumberExpr(_, n)  => n
      case NullExpr(_)       => null
      case VarExpr(_, user, Ident(pos, name)) =>
        if (user == "$") getVar(pos, name)
        else callFunction(pos, name, Nil)
      case ElementExpr(pos, globalvar, ids) =>
        lookupSeq(if (globalvar == "$") global else data, ids) match {
          case Some(value) => value
          case None        => ()
        }
      case BinaryExpr(left, "and", right) => beval(left) && beval(right)
      case BinaryExpr(left, "or", right)  => beval(left) || beval(right)
      case UnaryExpr("not", expr)         => !beval(expr)
      case BinaryExpr(left, op, right) =>
        val l = neval(left)
        val r = neval(right)

        op match {
          case "+"   => l + r
          case "-"   => l - r
          case "*"   => l * r
          case "/"   => l / r
          case "mod" => l remainder r
          case "\\"  => l quot r
          case "^"   => l.pow(r.toIntExact)
        }
      case UnaryExpr("-", expr) => -neval(expr)
      case MethodExpr(expr, Ident(pos, name), args) =>
        callFunction(pos, name, (args map eval) :+ eval(expr))
      case IndexExpr(expr, index) =>
        eval(expr) match {
          case m: Map[_, _] => m.asInstanceOf[Map[Any, _]](eval(index))
          case s: Seq[_]    => s(neval(index).toIntExact)
        }
      case ApplyExpr(Ident(pos, name), args) => callFunction(pos, name, args map eval)
      case PipeExpr(left, ApplyExpr(Ident(pos, name), args)) =>
        callFunction(pos, name, (args map eval) :+ eval(left))
    }

  private def lookup(v: Any, id: Ident): Option[Any] =
    v match {
      case null | ()               => None
      case m: collection.Map[_, _] => m.asInstanceOf[collection.Map[String, Any]] get id.name
      case p: Product              => p.productElementNames zip p.productIterator find { case (k, _) => k == id.name } map (_._2)
      case _                       => sys.error(s"not an object: $v")
    }

  @tailrec
  private def lookupSeq(v: Any, ids: Seq[Ident]): Option[Any] =
    ids.toList match {
      case Nil      => Some(v)
      case h :: Nil => lookup(v, h)
      case h :: t =>
        lookup(v, h) match {
          case Some(value) => lookupSeq(value, t)
          case None        => None
        }
    }

}
