package io.github.edadma.scemplate

import scala.annotation.tailrec
import scala.collection.mutable
import scala.language.postfixOps

object Renderer {

  private val ZERO = BigDecimal(0)

  val defaultRenderer = new Renderer(Builtin.functions)

}

class Renderer(builtins: Map[String, BuiltinFunction]) {

  import Renderer._

  private def lookup(v: Any, id: Ident): Option[Any] =
    v match {
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

  def render(globalContext: Any, ast: TemplateParserAST): String = {
    val vars = new mutable.HashMap[String, Any]

    def falsy(a: Any): Boolean =
      a match {
        case () | false | null | "" | ZERO          => true
        case s: collection.Iterable[_] if s.isEmpty => true
        case _                                      => false
      }

    def beval(context: Any, expr: ExprAST): Boolean = !falsy(eval(context, expr))

    def neval(context: Any, expr: ExprAST): BigDecimal =
      eval(context, expr) match {
        case n: BigDecimal => n
        case s: String     => BigDecimal(s)
        case v             => sys.error(s"not a number: $v")
      }

    def callFunction(pos: Int, name: String, args: Seq[Any]): Any =
      builtins get name match {
        case Some(BuiltinFunction(_, arities, function)) =>
          if (!arities(args.length)) {
            val arity =
              if (arities.size == 1) arities.head.toString
              else {
                val sorted = arities.toList.sorted

                s"${sorted dropRight 1 mkString ", "} or ${sorted.last}"
              }
            sys.error(s"wrong number of arguments for function '$name': expected $arity, found ${args.length}")
          } else if (!function.isDefinedAt(args))
            sys.error(s"cannot apply function '$name' to arguments ${args map (a => s"'$a'") mkString ", "}")
          else function(args)
        case None =>
          if (args.isEmpty) getVar(pos, name)
          else sys.error(s"function found: $name")
      }

    def getVar(pos: Int, name: String): Any =
      vars get name match {
        case Some(value) => value
        case None        => sys.error(s"unknown variable: $name")
      }

    def eval(context: Any, expr: ExprAST): Any =
      expr match {
        case ConditionalAST(cond, yes, no) =>
          if (beval(context, cond)) eval(context, yes)
          else if (no.isDefined) eval(context, no.get)
          else ""
        case CompareExpr(left, right) =>
          var l = neval(context, left)

          right forall {
            case (op, expr) =>
              val r = neval(context, expr)
              val res =
                op match {
                  case "<"  => l < r
                  case "<=" => l <= r
                  case ">"  => l > r
                  case ">=" => l >= r
                  case "="  => l == r
                  case "!=" => l != r
                }

              l = r
              res
          }
        case BooleanExpr(_, b) => b
        case StringExpr(_, s)  => s
        case NumberExpr(_, n)  => n
        case VarExpr(_, user, Ident(pos, name)) =>
          if (user == "$") getVar(pos, name)
          else callFunction(pos, name, Nil)
        case ElementExpr(pos, global, ids) =>
          lookupSeq(if (global == "$") globalContext else context, ids) match {
            case Some(value) => value
            case None        => () //sys.error(s"not found: .${ids map (_.name) mkString "."}")
          }
        case BinaryExpr(left, "and", right) => beval(context, left) && beval(context, right)
        case BinaryExpr(left, "or", right)  => beval(context, left) || beval(context, right)
        case UnaryExpr("not", expr)         => !beval(context, expr)
        case BinaryExpr(left, op, right) =>
          val l = neval(context, left)
          val r = neval(context, right)

          op match {
            case "+"   => l + r
            case "-"   => l - r
            case "*"   => l * r
            case "/"   => l / r
            case "mod" => l remainder r
            case "^"   => l.pow(r.toIntExact)
          }
        case UnaryExpr("-", expr) => -neval(context, expr)
        case MethodExpr(expr, Ident(pos, name), args) =>
          callFunction(pos, name, (args map (eval(context, _))) :+ eval(context, expr))
        case ApplyExpr(Ident(pos, name), args) => callFunction(pos, name, args map (eval(context, _)))
        case PipeExpr(left, ApplyExpr(Ident(pos, name), args)) =>
          callFunction(pos, name, (args map (eval(context, _))) :+ eval(context, left))
      }

    val buf = new StringBuilder

    def render(context: Any, ast: TemplateParserAST): Unit =
      ast match {
        case EmptyBlockAST    =>
        case SequenceAST(seq) => seq foreach (render(context, _))
        case BlockAST(WithAST(pos, expr), body, els) =>
          eval(context, expr) match {
            case v if falsy(v) => els foreach (render(context, _))
            case v             => render(v, body)
          }
        case BlockAST(ForAST(pos, index, expr), body, els) =>
          eval(context, expr) match {
            case v if falsy(v) => els foreach (render(context, _))
            case s: Iterable[Any] =>
              s.zipWithIndex foreach {
                case (e, i) =>
                  index match {
                    case Some((Some(Ident(_, idx)), Ident(_, elem))) =>
                      vars(idx) = i
                      vars(elem) = e
                    case Some((None, Ident(_, elem))) => vars(elem) = e
                    case None                         =>
                  }

                  render(e, body)
              }
            case v => sys.error(s"'for' can only be applied to an iterable object: $v")
          }
        case ContentAST(toks) =>
          toks foreach {
            case TextToken(pos, text)                                     => buf ++= text
            case SpaceToken(pos, s)                                       => buf ++= s
            case TagToken(pos, tag: ExprAST, _, _)                        => buf ++= eval(context, tag).toString
            case TagToken(pos, AssignmentAST(Ident(_, name), expr), _, _) => vars(name) = eval(context, expr).toString
          }
        case IfBlockAST(cond, yes, elseif, no) =>
          if (beval(context, cond)) render(context, yes)
          else {
            elseif find { case (c, _) => beval(context, c) } match {
              case Some(e) => render(context, e._2)
              case None    => no foreach (render(context, _))
            }
          }
      }

    render(globalContext, ast)
    buf.toString
  }

}
