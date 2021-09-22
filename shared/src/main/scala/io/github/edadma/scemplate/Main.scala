package io.github.edadma.scemplate

import java.io.File

import scopt.OParser
import pprint._

import io.github.edadma.cross_platform._
import platform._

object Main extends App {

  case class Config(dataFile: Option[String] = None,
                    dataString: Option[String] = None,
                    templateFile: Option[String] = None,
                    templateString: Option[String] = None)

  val builder = OParser.builder[Config]
  val parser = {
    import builder._

    OParser.sequence(
      programName("scemplate"),
      head("Scala Template Engine", "v0.1.0"),
      opt[Option[String]]('d', "data")
        .valueName("<YAML>")
        .optional()
        .action((d, c) => c.copy(dataString = d))
        .text("YAML document"),
      help('h', "help").text("prints this usage text"),
      opt[Option[String]]('t', "template")
        .valueName("<file>")
        .optional()
        .action((t, c) => c.copy(templateFile = t))
        .validate { t =>
          val f = new File(t.get)

          if (f.exists && f.isFile && f.canRead) success
          else failure("file must exist and be a readable file")
        }
        .text("template file"),
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
    case Some(Config(_, _, None, None)) => println(OParser.usage(parser))
    case Some(conf)                     => app(conf)
    case _                              =>
  }

  def app(c: Config): Unit = {
    val data: Any =
      if (c.dataFile.isDefined) yaml(readFile(c.dataFile.get))
      else if (c.dataString.isDefined) yaml(c.dataString.get)
      else Map()
    val template: String = {
      if (c.templateString.isDefined) c.templateString.get
      else if (c.templateFile.isDefined) readFile(c.templateFile.get)
      else ""
    }

    pprintln(data)
    println(template)
  }

}
