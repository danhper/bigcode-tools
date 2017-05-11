package com.tuvistavie.astgenerator.util

import com.tuvistavie.astgenerator.{CommandConfig, Config, GenerateAstConfig}

object CliParser {
  val parser = new scopt.OptionParser[Config]("ast-transformer") {
    head("ast-transformer", Config.version)

    cmd("generate-ast").action( (_, c) =>
      c.copy(command = "generate-ast", commandConfig = GenerateAstConfig())
    ).children(
      opt[Unit]("pretty").action { case (_, c @ Config(_, cc: GenerateAstConfig)) =>
      c.copy(commandConfig = cc.copy(pretty = true)) }.text("pretty format JSON"),

      opt[Unit]('k', "keep-identifiers").action { case (_, c @ Config(_, cc: GenerateAstConfig)) =>
        c.copy(commandConfig = cc.copy(keepIdentifiers = true)) }.text("keep program identifiers and values"),

      opt[String]('o', "output").required().action { case (x, c @ Config(_, cc: GenerateAstConfig)) =>
        c.copy(commandConfig = cc.copy(output = x)) }.text("file output"),

      arg[String]("<project>").action { case (x, c @ Config(_, cc: GenerateAstConfig)) =>
        c.copy(commandConfig = cc.copy(project = x) ) }.text("project to parse")
    )
  }

  def parse(args: Array[String]): Option[Config] = {
    parser.parse(args, Config())
  }
}
