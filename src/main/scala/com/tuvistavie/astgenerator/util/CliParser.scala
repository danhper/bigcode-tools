package com.tuvistavie.astgenerator.util

import com.tuvistavie.astgenerator.{Config, ExtractTokensConfig, GenerateAstConfig, NoConfig}

object CliParser {

  val parser = new scopt.OptionParser[Config]("ast-transformer") {
    head("ast-transformer", Config.version)

    cmd("generate-ast").action((_, _) => GenerateAstConfig()).children(
      opt[Unit]("pretty").action { (_, c) => (c: @unchecked) match { case c: GenerateAstConfig =>
        c.copy(pretty = true) } }.text("pretty format JSON"),

      opt[Unit]('k', "keep-identifiers").action { (_, c) => (c: @unchecked) match { case c: GenerateAstConfig =>
        c.copy(keepIdentifiers = true) } }.text("keep program identifiers and values"),

      opt[String]('o', "output").required().action { (x, c) => (c: @unchecked) match { case c: GenerateAstConfig =>
        c.copy(output = x) } }.text("file output"),

      arg[String]("<project>").action { (x, c) => (c: @unchecked) match { case c: GenerateAstConfig =>
        c.copy(project = x) } }.text("project to parse")
    )

    cmd("extract-tokens").action((_, _) => ExtractTokensConfig()).children(
      arg[String]("<project>").action { (x, c) => (c: @unchecked) match { case c: ExtractTokensConfig =>
        c.copy(project = x) } }.text("project to parse")
    )
  }

  def parse(args: Array[String]): Option[Config] = {
    parser.parse(args, NoConfig)
  }

  def showUsage(): Unit = parser.showUsage()
}
