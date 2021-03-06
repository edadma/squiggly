package io.github.edadma.squiggly

import java.io.PrintStream
import scala.annotation.tailrec
import scala.collection.mutable
import scala.language.postfixOps

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
  def callFunction(pos: TagParser#Position, name: String, args: Seq[Any]): Any =
    renderer.functions get name match {
      case Some(TemplateFunction(_, arity, function)) =>
        if (args.length < arity)
          pos.error(s"too few arguments for function '$name': expected $arity, found ${args.length}")
        else if (!function.isDefinedAt((this, args)))
          pos.error(s"cannot apply function '$name' to arguments ${args map (a => s"'$a'") mkString ", "}")
        else function((this, args))
      case None =>
        if (args.isEmpty) getVar(pos, name)
        else pos.error(s"function found: $name")
    }

  def getVar(pos: TagParser#Position, name: String): Any =
    vars get name match {
      case Some(value) => value
      case None        => pos.error(s"unknown variable: $name")
    }

  def beval(expr: ExprAST): Boolean = !falsy(eval(expr))

  def num(pos: TagParser#Position, v: Any): Num =
    v match {
      case n: Num => n
      case s: String =>
        try {
          BigDecimal(s)
        } catch {
          case _: NumberFormatException => pos.error(s"not a number: $s")
        }
    }

  def neval(pos: TagParser#Position, expr: ExprAST): Num = num(pos, eval(expr))

  def ieval(pos: TagParser#Position, expr: ExprAST): Int =
    try {
      neval(pos, expr).toIntExact
    } catch {
      case _: ArithmeticException => pos.error("must be an exact \"small\" integer")
    }

  def seval(pos: TagParser#Position, expr: ExprAST): String =
    eval(expr) match {
      case s: String => s
      case v         => pos.error(s"field name was expected: $v")
    }

  def eval(expr: ExprAST): Any =
    expr match {
      case e: NonStrictExpr => e
      case SeqExpr(elems)   => elems map eval
      case MapExpr(pairs)   => pairs map { case (TagParserIdent(_, k), pos, v) => (k, restrict(pos, eval(v))) } toMap
      case ConditionalAST(cond, yes, no) =>
        if (beval(cond)) eval(yes)
        else if (no.isDefined) eval(no.get)
        else ""
      case OrExpr(left, right)  => beval(left) || beval(right)
      case AndExpr(left, right) => beval(left) && beval(right)
      case CompareExpr(lpos, left, right) =>
        var l = eval(left)
        var lp = lpos

        right forall {
          case ("=", _, expr)  => l == eval(expr)
          case ("!=", _, expr) => l != eval(expr)
          case (op, rpos, expr) =>
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
                  val rn = num(rpos, r)

                  op match {
                    case "<"   => ln < rn
                    case "<="  => ln <= rn
                    case ">"   => ln > rn
                    case ">="  => ln >= rn
                    case "div" => (rn remainder ln) == ZERO
                  }
              }

            l = r
            lp = rpos
            res
        }

      case BooleanExpr(_, b)  => b
      case StringExpr(pos, s) => unescape(pos, s)
      case NumberExpr(_, n)   => n
      case NullExpr(_)        => null
      case VarExpr(_, user, TagParserIdent(pos, name)) =>
        if (user == "$") getVar(pos, name)
        else callFunction(pos, name, Nil)
      case ElementExpr(pos, globalvar, ids) =>
        lookupSeq(pos, if (globalvar == "$") global else data, ids) match {
          case Some(value) => value
          case None        => ()
        }
      case PrefixExpr("not", _, expr) => !beval(expr)
      case LeftInfixExpr(lpos, left, right) if right forall (_._1 == "++") =>
        val l = eval(left)
        val r = right map { case (_, _, e) => eval(e) }

        if (r forall (_.isInstanceOf[String])) r.asInstanceOf[Seq[String]].foldLeft(l.toString)(_ ++ _)
        else if (r forall (_.isInstanceOf[Seq[_]]))
          r.asInstanceOf[Seq[Seq[Any]]].foldLeft(l.asInstanceOf[Seq[Any]])(_ ++ _)
        else lpos.error("operands of '++' operator must all be either strings or sequences")
      case LeftInfixExpr(lpos, left, right) =>
        val l = neval(lpos, left)
        val r = right map { case (o, p, e) => (o, neval(p, e)) }

        r.foldLeft(l) {
          case (l, (o, r)) =>
            o match {
              case "+"   => l + r
              case "-"   => l - r
              case "*"   => l * r
              case "/"   => l / r
              case "mod" => l remainder r
              case "\\"  => l quot r
            }
        }
      case RightInfixExpr(lpos, left, "^", rpos, right) => neval(lpos, left) pow ieval(rpos, right)
      case PrefixExpr("-", pos, expr)                   => -neval(pos, expr)
      case MethodExpr(expr, id: TagParserIdent)         => lookup(id.pos, eval(expr), id) getOrElse ()
      case IndexExpr(expr, pos, index) =>
        eval(expr) match {
          case m: collection.Map[_, _] => m.asInstanceOf[collection.Map[Any, _]] getOrElse (eval(index), ())
          case s: collection.Seq[_] =>
            ieval(pos, index) match {
              case n if n < 0         => pos.error(s"negative array index: $n")
              case n if n >= s.length => pos.error(s"array index out of bounds: $n")
              case n                  => s(n)
            }
          case p: Product =>
            p.productElementNames zip p.productIterator find { case (k, _) => k == seval(pos, index) } map (_._2) getOrElse ()
          case s: String =>
            ieval(pos, index) match {
              case n if n < 0         => pos.error(s"negative array index: $n")
              case n if n >= s.length => pos.error(s"array index out of bounds: $n")
              case n                  => s(n).toString
            }
          case v => pos.error(s"not indexable: $v")
        }
      case ApplyExpr(TagParserIdent(pos, name), args) => callFunction(pos, name, args map eval)
      case PipeExpr(left, ApplyExpr(TagParserIdent(pos, name), args)) =>
        callFunction(pos, name, (args map eval) :+ eval(left))
    }

  private def lookup(pos: TagParser#Position, v: Any, id: TagParserIdent): Option[Any] = {
    def tryMethod: Option[Any] =
      if (renderer.methods contains id.name)
        Some(callFunction(id.pos, id.name, Seq(v)))
      else
        None

    v match {
      case ()                      => pos.error(s"attempt to lookup property '${id.name}' of undefined")
      case null                    => None
      case m: collection.Map[_, _] => m.asInstanceOf[collection.Map[String, Any]] get id.name orElse tryMethod
      case p: Product =>
        p.productElementNames zip p.productIterator find {
          case (k, _) => k == id.name
        } map (_._2) orElse tryMethod
      case _ => tryMethod orElse pos.error(s"not an object (i.e., Map or case class): $v")
    }
  }

  @tailrec
  private def lookupSeq(pos: TagParser#Position, v: Any, ids: Seq[TagParserIdent]): Option[Any] =
    ids.toList match {
      case Nil      => Some(v)
      case h :: Nil => lookup(pos, v, h)
      case h :: t =>
        lookup(pos, v, h) match {
          case Some(value) => lookupSeq(pos, value, t)
          case None        => None
        }
    }

}
