package io.github.edadma.squiggly

import java.io.PrintStream

import scala.collection.mutable
import scala.language.postfixOps

object Renderer {

  val default = new Renderer()

}

class Renderer(protected[squiggly] val partials: PartialsLoader = _ => None,
               protected[squiggly] val blocks: Blocks = new mutable.HashMap[String, ParserAST],
               protected[squiggly] val functions: Map[String, BuiltinFunction] = Builtin.functions) {

  def render(globalData: Any, ast: ParserAST, out: PrintStream = Console.out): Any = {
    val globalContext = Context(this, globalData, new mutable.HashMap[String, Any], out)
    var returnValue: Any = ()

    globalContext.global = globalData

    def render(context: Context, ast: ParserAST): Unit = {
      ast match {
        case EmptyBlockAST                          =>
        case SequenceAST(seq)                       => seq foreach (render(context, _))
        case DefineBlockAST(Ident(pos, name), body) => blocks(name) = body
        case BlockBlockAST(Ident(pos, name), body, expr) =>
          render(context.copy(data = restrict(pos, context.eval(expr))), blocks.getOrElse(name, body))
        case TemplateBlockAST(tpos, WithAST(_, expr), body, els) =>
          context.eval(expr) match {
            case v if falsy(v) => els foreach (render(context, _))
            case v             => render(context.copy(data = v), body)
          }
        case TemplateBlockAST(_, ForAST(index, pos, expr), body, els) =>
          context.eval(expr) match {
            case v if falsy(v) => els foreach (render(context, _))
            case s: Iterable[Any] =>
              s.zipWithIndex foreach {
                case (e, i) =>
                  index match {
                    case Some((Ident(_, elem), Some(Ident(_, idx)))) =>
                      context.vars(idx) = i
                      context.vars(elem) = e
                    case Some((Ident(_, elem), None)) => context.vars(elem) = e
                    case None                         =>
                  }

                  render(context.copy(data = e), body)
              }
            case v => pos.error(s"'for' can only be applied to an iterable object: $v")
          }
        case ContentAST(toks) =>
          toks foreach {
            case TextToken(_, text) => out print text
            case SpaceToken(_, s)   => out print s
            case TagToken(_, tag: ExprAST, _, _) =>
              out print
                (context.eval(tag) match {
                  case null | () => ""
                  case v         => v.toString
                })
            case TagToken(_, AssignmentAST(Ident(_, name), expr), _, _) => context.vars(name) = context.eval(expr)
            case TagToken(_, ReturnAST(expr), _, _) =>
              returnValue = expr map context.eval getOrElse ()
              throw new ReturnException
            case TagToken(_, _: CommentAST, _, _) =>
          }
        case IfBlockAST(cond, yes, elseif, no) =>
          if (context.beval(cond)) render(context, yes)
          else {
            elseif find { case (c, _) => context.beval(c) } match {
              case Some(e) => render(context, e._2)
              case None    => no foreach (render(context, _))
            }
          }
        case MatchBlockAST(expr, cases, els) =>
          val value = context.eval(expr)

          cases find { case (c, _) => context.eval(c) == value } match {
            case Some(e) => render(context, e._2)
            case None    => els foreach (render(context, _))
          }
      }
    }

    try {
      render(globalContext, ast)
    } catch {
      case _: ReturnException =>
    }

    returnValue
  }

}
