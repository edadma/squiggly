package io.github.edadma.scemplate

import scala.annotation.tailrec
import scala.collection.immutable.{AbstractSeq, LinearSeq}

//object Renderer {
//
//  val defaultRenderer = new Renderer
//
//}

class Renderer {

  private def lookup(v: Any, id: Ident): Option[Any] =
    v match {
      case m: collection.Map[_, _] =>
        m.asInstanceOf[collection.Map[String, Any]] get id.name
      case p: Product => p.productElementNames zip p.productIterator find { case (k, _) => k == id.name } map (_._2)
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
    def beval(context: Any, expr: ExprAST): Boolean = eval(context, expr).asInstanceOf[Boolean]

    def neval(context: Any, expr: ExprAST): BigDecimal = eval(context, expr).asInstanceOf[BigDecimal]

    def eval(context: Any, expr: ExprAST): Any =
      expr match {
        case StringExpr(_, s)                   => s
        case NumberExpr(_, n)                   => n
        case VarExpr(_, user, Ident(pos, name)) =>
        case ElementExpr(pos, global, ids) =>
          lookupSeq(if (global == "$") globalContext else context, ids) match {
            case Some(value) => value
            case None        => sys.error(s"not found: .${ids mkString "."}")
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
      }

    val buf = new StringBuilder

    def render(context: Any, ast: TemplateParserAST): Unit =
      ast match {
        case SequenceAST(seq)                   => seq foreach (render(context, _))
        case BlockAST(WithAST(pos, expr), body) => render(eval(context, expr), body)
        case ContentAST(toks) =>
          toks foreach {
            case TextToken(pos, text)              => buf ++= text
            case SpaceToken(pos, s)                => buf ++= s
            case TagToken(pos, tag: ExprAST, _, _) => buf ++= eval(context, tag).toString
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
