package io.github.edadma.squiggly

import java.io.PrintStream

import scala.collection.mutable
import scala.language.postfixOps

object Renderer {

  val basic = new Renderer()

}

class Renderer(private[squiggly] val out: PrintStream = Console.out,
               private[squiggly] val partials: PartialsLoader = _ => None,
               private[squiggly] val functions: Map[String, BuiltinFunction] = Builtin.functions) {

  def render(globalData: Any, ast: TemplateParserAST): Any = {
    val globalContext = Context(this, globalData, new mutable.HashMap[String, Any])
    var returnValue: Any = ()

    globalContext.global = globalData

    def render(context: Context, ast: TemplateParserAST): Unit = {
      ast match {
        case EmptyBlockAST    =>
        case SequenceAST(seq) => seq foreach (render(context, _))
        case BlockAST(tpos, WithAST(_, expr), body, els) =>
          context.eval(expr) match {
            case v if falsy(v) => els foreach (render(context, _))
            case v             => render(context.copy(data = v), body)
          }
        case BlockAST(_, ForAST(index, pos, expr), body, els) =>
          context.eval(expr) match {
            case v if falsy(v) => els foreach (render(context, _))
            case s: Iterable[Any] =>
              s.zipWithIndex foreach {
                case (e, i) =>
                  index match {
                    case Some((Some(Ident(_, idx)), Ident(_, elem))) =>
                      context.vars(idx) = i
                      context.vars(elem) = e
                    case Some((None, Ident(_, elem))) => context.vars(elem) = e
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
