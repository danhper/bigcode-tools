package com.tuvistavie.astgenerator.util

import com.tuvistavie.astgenerator.models._

object CliParser {

  val parser = new scopt.OptionParser[Config]("ast-transformer") {
    head("ast-transformer", Config.version)

    cmd("generate-ast").action((_, _) => GenerateAstConfig()).children(
      opt[Unit]("pretty").action { (_, c) => (c: @unchecked) match { case c: GenerateAstConfig =>
        c.copy(pretty = true) } }.text("pretty format JSON"),

      opt[Unit]('k', "keep-identifiers").action { (_, c) => (c: @unchecked) match { case c: GenerateAstConfig =>
        c.copy(keepIdentifiers = true) } }.text("keep program identifiers and values"),

      opt[String]('o', "output").required().action { (x, c) => (c: @unchecked) match { case c: GenerateAstConfig =>
        c.copy(output = x) } }.text("output file"),

      arg[String]("<project>").action { (x, c) => (c: @unchecked) match { case c: GenerateAstConfig =>
        c.copy(project = x) } }.text("project to parse")
    )

    cmd("extract-tokens").action((_, _) => ExtractTokensConfig()).children(
      arg[String]("<project>").action { (x, c) => (c: @unchecked) match { case c: ExtractTokensConfig =>
        c.copy(project = x) } }.text("project to parse")
    )

    cmd("generate-dot").action((_, _) => GenerateDotConfig()).children(
      arg[String]("<filepath>").action { (x, c) => (c: @unchecked) match { case c: GenerateDotConfig =>
        c.copy(filepath = x) } }.text("file to parse"),
      opt[String]('o', "output").action { (x, c) => (c: @unchecked) match { case c: GenerateDotConfig =>
        c.copy(output = Some(x)) } }.text("output file"),
      opt[Unit]('s', "silent").action { (_, c) => (c: @unchecked) match { case c: GenerateDotConfig =>
        c.copy(silent = true) } }.text("do not output dot to stdout"),
      opt[Unit]("hide-identifiers").action { (_, c) => (c: @unchecked) match { case c: GenerateDotConfig =>
        c.copy(hideIdentifiers = true) } }.text("do not show tokens")
    )

    cmd("generate-vocabulary").action((_, _) => GenerateVocabularyConfig()).children(
      arg[String]("<project>").action { (x, c) => (c: @unchecked) match { case c: GenerateVocabularyConfig =>
        c.copy(project = x) } }.text("project from which vocabulary should be generated"),
      opt[Seq[Int]]('d', "depth").valueName("<depth1>,<depth2>").action { (x, c) => (c: @unchecked) match { case c: GenerateVocabularyConfig =>
        c.copy(depths = x) } }.text("the depth of the extracted subgraphs"),
      opt[String]('o', "output").action { (x, c) => (c: @unchecked) match { case c: GenerateVocabularyConfig =>
        c.copy(output = Some(x)) } }.text("output file"),
      opt[Unit]('s', "silent").action { (_, c) => (c: @unchecked) match { case c: GenerateVocabularyConfig =>
        c.copy(silent = true) } }.text("do not output info to stdout")
    )
  }

  def parse(args: Array[String]): Option[Config] = {
    parser.parse(args, NoConfig)
  }

  def showUsage(): Unit = parser.showUsage()
}
