package io.github.edadma.squiggly

import scopt.OParser
import io.github.edadma.cross_platform._

@main def run(args: String*): Unit =

  case class Config(
      dataFile: Option[String] = None,
      dataString: Option[String] = None,
      templateFile: Option[String] = None,
      templateString: Option[String] = None,
      ast: Boolean = false,
  )

  val builder = OParser.builder[Config]
  val parser =
    import builder._

    OParser.sequence(
      programName("squiggly"),
      head("Squiggly Template Engine", "v0.2.2"),
      opt[Unit]('a', "ast")
        .optional()
        .action((_, c) => c.copy(ast = true))
        .text("pretty print AST"),
      opt[String]('d', "data")
        .valueName("<JSON>")
        .optional()
        .action((d, c) => c.copy(dataString = Some(d)))
        .text("JSON document (string)"),
      opt[String]('f', "template")
        .valueName("<file>")
        .optional()
        .action((f, c) => c.copy(templateFile = Some(f)))
        .validate(t => if readableFile(t) then success else failure("file must exist and be a readable file"))
        .text("template file"),
      help('h', "help").text("prints this usage text"),
      version('v', "version").text("prints the version"),
      opt[String]('j', "json")
        .valueName("<file>")
        .optional()
        .action((j, c) => c.copy(dataFile = Some(j)))
        .validate(t => if readableFile(t) then success else failure("file must exist and be a readable file"))
        .text("JSON data file"),
      arg[String]("[<template>]")
        .optional()
        .action((t, c) => c.copy(templateString = Some(t)))
        .text("template string"),
    )

  // cross_platform.processArgs: on JVM and Native this is just `args`
  // verbatim; on Scala.js it reads the real argv from `process.argv`
  // (skipping `node` and the script path) because Scala.js's
  // `scalaJSUseMainModuleInitializer := true` invokes @main at module-load
  // with no sbt-supplied arguments. This is what makes the linked .js work
  // when launched as `node main.js arg1 arg2 ...`.
  OParser.parse(parser, processArgs(args), Config()) match
    case Some(Config(_, _, None, None, _)) => println(OParser.usage(parser))
    case Some(conf)                        => app(conf)
    case _                                 =>

  def app(c: Config): Unit =
    val data: Any =
      if c.dataFile.isDefined then parseJsonData(readFile(c.dataFile.get))
      else if c.dataString.isDefined then parseJsonData(c.dataString.get)
      else Map.empty[String, Any]
    val template: String =
      if c.templateString.isDefined then c.templateString.get
      else if c.templateFile.isDefined then readFile(c.templateFile.get)
      else ""

    val ast = new TemplateParser().parse(template)

    if c.ast then println(ast)
    else
      TemplateRenderer.default.render(data, ast)
      println()
