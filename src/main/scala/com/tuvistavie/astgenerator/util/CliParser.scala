package com.tuvistavie.astgenerator.util

import com.tuvistavie.astgenerator.Config

object CliParser {
  val parser = new scopt.OptionParser[Config]("ast-transformer") {
    head("ast-transformer", Config.version)

    opt[Unit]("pretty").action( (_, c) =>
      c.copy(pretty = true) ).text("pretty format JSON")

    opt[String]('o', "output").required().action( (x, c) =>
      c.copy(output = x) ).text("file output")

    arg[String]("<project>").action( (x, c) =>
      c.copy(project = x) ).text("project to parse")
  }

  def parse(args: Array[String]): Option[Config] = {
    parser.parse(args, Config())
  }
}
