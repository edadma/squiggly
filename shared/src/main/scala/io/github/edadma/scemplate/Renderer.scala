package io.github.edadma.scemplate

//object Renderer {
//
//  val defaultRenderer = new Renderer
//
//}

class Renderer {

  def render(data: Any, ast: TemplateParserAST): String = {
    val buf = new StringBuilder

    def render(ast: TemplateParserAST): Unit =
      ast match {
        case BlockAST(block, body) => ???
        case BodyAST(toks) =>
          toks foreach {
            case TextToken(pos, text)                    => buf ++= text
            case SpaceToken(pos, s)                      => buf ++= s
            case TagToken(pos, tag, trimLeft, trimRight) =>
          }
        case IfBlockAST(cond, yes, elseif, no) => ???
      }

    render(ast)
    buf.toString
  }
}
