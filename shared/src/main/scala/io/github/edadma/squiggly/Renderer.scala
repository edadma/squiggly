package io.github.edadma.squiggly

import io.github.edadma.char_reader.CharReader

import java.io.PrintStream

import scala.collection.mutable
import scala.language.postfixOps

object Renderer {

  val defaultRenderer = new Renderer(Console.out, Builtin.functions)

}

class Renderer(out: PrintStream, functions: Map[String, BuiltinFunction]) {

  def render(globalData: Any, ast: TemplateParserAST): Any = {
    val globalContext = Context(globalData, out, functions, new mutable.HashMap[String, Any])
    val returnValue: Any = ()

    globalContext.global = globalData

    def render(context: Context, ast: TemplateParserAST): Unit = {
      def restrict(pos: CharReader, v: Any): Any =
        v match {
          case () => pos.error("attempting to bind a value of 'undefined'")
          case _  => v
        }

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
            case TagToken(pos, AssignmentAST(Ident(_, name), expr), _, _) =>
              context.vars(name) = context.eval(expr)
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
