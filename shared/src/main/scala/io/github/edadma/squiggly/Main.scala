package io.github.edadma.squiggly

import java.io.File
import scopt.OParser
import io.github.edadma.cross_platform._
import platform._
import pprint.pprintln

object Main extends App {

  case class Config(dataFile: Option[String] = None,
                    dataString: Option[String] = None,
                    templateFile: Option[String] = None,
                    templateString: Option[String] = None,
                    ast: Boolean = false)

  val builder = OParser.builder[Config]
  val parser = {
    import builder._

    // todo: add -t templatename=file,...
    // todo: add -v varname=value,...

    val BOLD = Console.BOLD
    var firstSection = true

    def section(name: String) = {
      val res =
        s"${if (!firstSection) "\n" else ""}$BOLD\u2501\u2501\u2501\u2501\u2501 $name ${"\u2501" * (25 - name.length)}${Console.RESET}"

      firstSection = false
      res
    }

    OParser.sequence(
      programName("squiggly"),
      head("Squiggly Template Engine", "v0.1.10"),
      note(section("first section")),
      opt[Unit]('a', "ast")
        .optional()
        .action((_, c) => c.copy(ast = true))
        .text("pretty print AST"),
      opt[Option[String]]('d', "data")
        .valueName("<YAML>")
        .optional()
        .action((d, c) => c.copy(dataString = d))
        .text("YAML document"),
      note(section("asdf")),
      opt[Option[String]]('f', "template")
        .valueName("<file>")
        .optional()
        .action((f, c) => c.copy(templateFile = f))
        .validate { t =>
          val f = new File(t.get)

          if (f.exists && f.isFile && f.canRead) success
          else failure("file must exist and be a readable file")
        }
        .text("template file"),
      note(section("asdf dfgh")),
      help('h', "help").text("prints this usage text"),
      version('v', "version").text("prints the version"),
      opt[Option[String]]('y', "yaml")
        .valueName("<file>")
        .optional()
        .action((y, c) => c.copy(dataFile = y))
        .validate { t =>
          val f = new File(t.get)

          if (f.exists && f.isFile && f.canRead) success
          else failure("file must exist and be a readable file")
        }
        .text("YAML data file"),
      arg[Option[String]]("[<template>]")
        .optional()
        .action((t, c) => c.copy(templateString = t))
        .text(s"template string")
    )
  }

  OParser.parse(parser, args, Config()) match {
    case Some(Config(_, _, None, None, _)) => println(OParser.usage(parser))
    case Some(conf)                        => app(conf)
    case _                                 =>
  }

  def app(c: Config): Unit = {
    val data: Any =
      if (c.dataFile.isDefined) yaml(readFile(c.dataFile.get))
      else if (c.dataString.isDefined) yaml(c.dataString.get)
      else Map()
    val template: String = {
      if (c.templateString.isDefined) c.templateString.get
      else if (c.templateFile.isDefined)
        if (c.templateFile.get == "--") scala.io.Source.fromInputStream(System.in).mkString
        else readFile(c.templateFile.get)
      else ""
    }

    val ast = new TemplateParser().parse(template)

    if (c.ast)
      pprintln(ast)
    else {
      TemplateRenderer.default.render(data, ast)
      println()
    }
  }

}
