package io.github.edadma.squiggly

import java.io.PrintStream
import scala.annotation.tailrec
import scala.collection.mutable
import scala.language.postfixOps
import scala.util.parsing.input.Positional

case class Context(renderer: TemplateRenderer, data: Any, vars: mutable.HashMap[String, Any], out: PrintStream) {

  private var _global: Any = _

  def global_=(d: Any): Unit = {
    require(_global == null)
    _global = d
  }

  def global: Any = {
    require(_global != null)
    _global
  }

  // todo: arguments should have Position for error reporting
  def callFunction(id: Ident, args: Seq[Any]): Any =
    renderer.functions get id.name match {
      case Some(TemplateFunction(_, arity, function)) =>
        if (args.length < arity)
          problem(id, s"too few arguments for function '${id.name}': expected $arity, found ${args.length}")
        else if (!function.isDefinedAt((this, args)))
          problem(id, s"cannot apply function '${id.name}' to arguments ${args map (a => s"'$a'") mkString ", "}")
        else function((this, args))
      case None =>
        if (args.isEmpty) getVar(id, id.name)
        else problem(id, s"function found: ${id.name}")
    }

  def getVar(pos: Positional, name: String): Any =
    vars get name match {
      case Some(value) => value
      case None        => problem(pos, s"unknown variable: $name")
    }

  def beval(expr: ExprAST): Boolean = !falsy(eval(expr))

  def num(pos: Positional, v: Any): Num =
    v match {
      case n: Num => n
      case s: String =>
        try {
          BigDecimal(s)
        } catch {
          case _: NumberFormatException => problem(pos, s"not a number: $s")
        }
    }

  def neval(expr: ExprAST): Num = num(expr, eval(expr))

  def ieval(expr: ExprAST): Int =
    try {
      neval(expr).toIntExact
    } catch {
      case _: ArithmeticException => problem(expr, "must be an exact \"small\" integer")
    }

  def seval(expr: ExprAST): String =
    eval(expr) match {
      case s: String => s
      case v         => problem(expr, s"field name was expected: $v")
    }

  def eval(expr: ExprAST): Any =
    expr match {
      case e: NonStrictExpr => e
      case SeqExpr(elems)   => elems map eval
      case MapExpr(pairs)   => pairs map { case (pos @ Ident(k), v) => (k, restrict(pos, eval(v))) } toMap
      case ConditionalAST(cond, yes, no) =>
        if (beval(cond)) eval(yes)
        else if (no.isDefined) eval(no.get)
        else ""
      case OrExpr(left, right)  => beval(left) || beval(right)
      case AndExpr(left, right) => beval(left) && beval(right)
      case CompareExpr(left, right) =>
        var l = eval(left)
        var lp = left

        right forall {
          case ("=", expr)  => l == eval(expr)
          case ("!=", expr) => l != eval(expr)
          case (op, expr) =>
            val r = eval(expr)
            val res =
              (l, r) match {
                case (l: String, r: String) =>
                  op match {
                    case "<"  => l < r
                    case "<=" => l <= r
                    case ">"  => l > r
                    case ">=" => l >= r
                  }
                case _ =>
                  val ln = num(lp, l)
                  val rn = num(expr, r)

                  op match {
                    case "<"   => ln < rn
                    case "<="  => ln <= rn
                    case ">"   => ln > rn
                    case ">="  => ln >= rn
                    case "div" => (rn remainder ln) == ZERO
                  }
              }

            l = r
            lp = expr
            res
        }
      case BooleanExpr(b)      => b
      case pos @ StringExpr(s) => unescape(pos, s)
      case NumberExpr(n)       => n
      case NullExpr()          => null
      case VarExpr(user, id) =>
        if (user == "$") getVar(id, id.name)
        else callFunction(id, Nil)
      case ElementExpr(globalvar, ids) =>
        lookupSeq(if (globalvar == "$") global else data, ids) match {
          case Some(value) => value
          case None        => ()
        }
      case PrefixExpr("not", expr) => !beval(expr)
      case LeftInfixExpr(left, "++", right) =>
        val l = eval(left)
        val r = eval(right)

        r match
          case s: String   => l.toString ++ s
          case seq: Seq[_] => l.asInstanceOf[Seq[Any]] ++ seq
          case _           => problem(left, "operands of '++' operator must all be either strings or sequences")
      case LeftInfixExpr(left, o, right) =>
        val l = neval(left)
        val r = neval(right)

        o match {
          case "+"   => l + r
          case "-"   => l - r
          case "*"   => l * r
          case "/"   => l / r
          case "mod" => l remainder r
          case "\\"  => l quot r
        }
      case RightInfixExpr(left, "^", right) => neval(left) pow ieval(right)
      case PrefixExpr("-", expr)            => -neval(expr)
      case MethodExpr(expr, id: Ident)      => lookup(eval(expr), id) getOrElse ()
      case IndexExpr(expr, index) =>
        eval(expr) match {
          case m: collection.Map[_, _] => m.asInstanceOf[collection.Map[Any, _]] getOrElse (eval(index), ())
          case s: collection.Seq[_] =>
            ieval(index) match {
              case n if n < 0         => problem(index, s"negative array index: $n")
              case n if n >= s.length => problem(index, s"array index out of bounds: $n")
              case n                  => s(n)
            }
          case p: Product =>
            p.productElementNames zip p.productIterator find { case (k, _) =>
              k == seval(index)
            } map (_._2) getOrElse ()
          case s: String =>
            ieval(index) match {
              case n if n < 0         => problem(index, s"negative array index: $n")
              case n if n >= s.length => problem(index, s"array index out of bounds: $n")
              case n                  => s(n).toString
            }
          case v => problem(index, s"not indexable: $v")
        }
      case ApplyExpr(name, args) => callFunction(name, args map eval)
      case PipeExpr(left, ApplyExpr(name, args)) =>
        callFunction(name, (args map eval) :+ eval(left))
    }
  private def lookup(v: Any, id: Ident): Option[Any] = {
    def tryMethod: Option[Any] =
      if (renderer.methods contains id.name)
        Some(callFunction(id, Seq(v)))
      else
        None

    v match {
      case ()                      => problem(id, s"attempt to lookup property '${id.name}' of undefined")
      case null                    => None
      case m: collection.Map[_, _] => m.asInstanceOf[collection.Map[String, Any]] get id.name orElse tryMethod
      case p: Product =>
        p.productElementNames zip p.productIterator find { case (k, _) =>
          k == id.name
        } map (_._2) orElse tryMethod
      case _ => tryMethod orElse problem(id, s"not an object (i.e., Map or case class): $v")
    }
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
