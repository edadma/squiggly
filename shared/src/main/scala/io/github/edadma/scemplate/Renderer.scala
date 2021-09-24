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
    ids match {
      case Nil      => Some(v)
      case h :: Nil => lookup(v, h)
      case h :: t =>
        lookup(v, h) match {
          case Some(value) => lookupSeq(value, t)
          case None        => None
        }
    }

  def eval(data: Any, expr: ExprAST): Any =
    expr match {
      case StringExpr(_, s)                   => s
      case NumberExpr(_, n)                   => n
      case VarExpr(_, user, Ident(pos, name)) =>
      case ElementExpr(pos, ids)              =>
      case BinaryExpr(left, "and", right) =>
        eval(data, left)

      case BinaryExpr(left, op, right) =>
        val l = eval(data, left)
        val r = eval(data, right)

        op match {
          case "+" =>
        }
    }

  def render(data: Any, ast: TemplateParserAST): String = {
    val buf = new StringBuilder

    def render(data: Any, ast: TemplateParserAST): Unit =
      ast match {
        case BlockAST(WithAST(pos, expr), body) =>
        case ContentAST(toks) =>
          toks foreach {
            case TextToken(pos, text) => buf ++= text
            case SpaceToken(pos, s)   => buf ++= s
            case TagToken(pos, ElementExpr(_, ids), _, _) =>
              lookupSeq(data, ids) match {
                case Some(value) => buf ++= value.toString
                case None        => pos.error(s"not found")
              }
            case TagToken(pos, tag: ExprAST, _, _) => buf ++= eval(data, tag).toString
          }
        case IfBlockAST(cond, yes, elseif, no) => ???
      }

    render(data, ast)
    buf.toString
  }

}
