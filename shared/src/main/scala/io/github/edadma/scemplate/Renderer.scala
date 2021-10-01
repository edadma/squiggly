package io.github.edadma.scemplate

import scala.collection.mutable
import scala.language.postfixOps

object Renderer {

  val defaultRenderer = new Renderer(Builtin.functions)

}

class Renderer(functions: Map[String, BuiltinFunction]) {

  def render(globalData: Any, ast: TemplateParserAST): String = {
    val globalContext = Context(globalData, functions, new mutable.HashMap[String, Any])

    globalContext.global = globalData

    val buf = new StringBuilder

    def render(context: Context, ast: TemplateParserAST): Unit =
      ast match {
        case EmptyBlockAST    =>
        case SequenceAST(seq) => seq foreach (render(context, _))
        case BlockAST(WithAST(pos, expr), body, els) =>
          context.eval(expr) match {
            case v if falsy(v) => els foreach (render(context, _))
            case v             => render(context.copy(data = v), body)
          }
        case BlockAST(ForAST(pos, index, expr), body, els) =>
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
            case v => sys.error(s"'for' can only be applied to an iterable object: $v")
          }
        case ContentAST(toks) =>
          toks foreach {
            case TextToken(pos, text)                                     => buf ++= text
            case SpaceToken(pos, s)                                       => buf ++= s
            case TagToken(pos, tag: ExprAST, _, _)                        => buf ++= context.eval(tag).toString
            case TagToken(pos, AssignmentAST(Ident(_, name), expr), _, _) => context.vars(name) = context.eval(expr)
            case TagToken(pos, tag: CommentAST, _, _)                     =>
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

    render(globalContext, ast)
    buf.toString
  }

}
