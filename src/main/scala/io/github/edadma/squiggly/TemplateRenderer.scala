package io.github.edadma.squiggly

import java.io.{OutputStream, PrintStream}
import scala.collection.{MapView, mutable}
import scala.language.postfixOps

object TemplateRenderer {

  val default = new TemplateRenderer()

}

class TemplateRenderer(
    val partials: TemplateLoader = _ => None,
    val blocks: Blocks = new mutable.HashMap[String, TemplateAST],
    val functions: Map[String, TemplateFunction] = TemplateBuiltin.functions,
    val data: Map[String, Any] = Map(),
) {

  val methods: MapView[String, TemplateFunction] =
    functions.view.filter { case (_, TemplateFunction(_, arity, _)) =>
      arity == 1
    }

  def render(globalData: Any, ast: TemplateAST, out: OutputStream = Console.out): Any = {
    val pout =
      out match {
        case p: PrintStream => p
        case _              => new PrintStream(out)
      }
    val globalContext = Context(this, globalData, new mutable.HashMap[String, Any], pout)
    var returnValue: Any = ()

    globalContext.global = globalData

    def render(context: Context, ast: TemplateAST): Unit = {
      ast match {
        case EmptyBlockAST                     =>
        case SequenceAST(seq)                  => seq foreach (render(context, _))
        case DefineBlockAST(Ident(name), body) => blocks(name) = body
        case BlockBlockAST(pos @ Ident(name), body, expr) =>
          render(context.copy(data = restrict(pos, context.eval(expr))), blocks.getOrElse(name, body))
        case TemplateBlockAST(tpos, WithAST(expr), body, els) =>
          context.eval(expr) match {
            case v if falsy(v) => els foreach (render(context, _))
            case v             => render(context.copy(data = v), body)
          }
        case TemplateBlockAST(_, ForAST(index, expr), body, els) =>
          context.eval(expr) match {
            case v if falsy(v) => els foreach (render(context, _))
            case s: collection.Seq[_] =>
              s.zipWithIndex foreach { case (e, i) =>
                index match {
                  case Some((Ident(elem), Some(Ident(idx)))) =>
                    context.vars(idx) = i
                    context.vars(elem) = e
                  case Some((Ident(elem), None)) => context.vars(elem) = e
                  case None                      =>
                }

                render(context.copy(data = e), body)
              }
            case s: collection.Map[_, _] =>
              s foreach { case (k, v) =>
                index match {
                  case Some((Ident(key), Some(Ident(value)))) =>
                    context.vars(key) = k
                    context.vars(value) = v
                  case Some((Ident(value), None)) => context.vars(value) = v
                  case None                       =>
                }

                render(context.copy(data = v), body)
              }
            case v => error(expr, s"'for' can only be applied to an iterable object: $v")
          }
        case ContentAST(toks) =>
          toks foreach {
            case TextToken(_, text) => pout print text
            case SpaceToken(_, s)   => pout print s
            case TagToken(_, tag: ExprAST, _, _) =>
              def render(v: Any): String =
                v match {
                  case s: collection.Seq[_]    => s.mkString("[", ", ", "]")
                  case m: collection.Map[_, _] => m map { case (k, v) => s"$k: ${render(v)}" } mkString ("[", ", ", "]")
                  case s: String               => s""""$s""""
                  case null | ()               => ""
                  case v                       => v.toString
                }

              pout print
                (context.eval(tag) match {
                  case s: String => s
                  case v         => render(v)
                })
            case TagToken(_, AssignmentAST(name, expr), _, _) => context.vars(name) = context.eval(expr)
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
