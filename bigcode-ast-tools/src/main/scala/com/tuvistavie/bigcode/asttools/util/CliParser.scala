package com.tuvistavie.bigcode.asttools.util

import com.tuvistavie.bigcode.asttools.models._
import scopt.OptionParser

object CliParser {
  val parser: OptionParser[Config] = new scopt.OptionParser[Config]("bigcode-ast-tools") {

    head("bigcode-ast-tools", Config.version)

    opt[Unit]('h', "help").action((_, _) => ShowHelpConfig).text("shows help")
    opt[Unit]('v', "version").action((_, _) => ShowVersionConfig).text("shows version")

    cmd("visualize-ast").action((_, _) => VisualizeAstConfig()).children(
      arg[String]("<filepath>").action { (x, c) => (c: @unchecked) match { case c: VisualizeAstConfig =>
        c.copy(input = x) } }.text("file to parse"),
      opt[String]('o', "output").action { (x, c) => (c: @unchecked) match { case c: VisualizeAstConfig =>
        c.copy(output = Some(x)) } }.text("output file"),
      opt[Unit]("debug").action { (_, c) => (c: @unchecked) match { case c: VisualizeAstConfig =>
        c.copy(debug = true) } }.text("output dot to stdout"),
      opt[Unit]("hide-identifiers").action { (_, c) => (c: @unchecked) match { case c: VisualizeAstConfig =>
        c.copy(hideIdentifiers = true) } }.text("do not show tokens"),
      opt[Unit]("no-open").action { (_, c) => (c: @unchecked) match { case c: VisualizeAstConfig =>
        c.copy(view = false) } }.text("do not open generated file"),
      opt[Int]('i', "index").action { (v, c) => (c: @unchecked) match { case c: VisualizeAstConfig =>
        c.copy(index = Some(v)) } }.text("the index of the AST to output"),
      opt[String]('l', "files-list").action { (v, c) => (c: @unchecked) match { case c: VisualizeAstConfig =>
        c.copy(filesListPath = Some(v)) } }.text("the path to the list of file names (only useful when using --filename)"),
      opt[String]('f', "filename").action { (v, c) => (c: @unchecked) match { case c: VisualizeAstConfig =>
        c.copy(filename = Some(v)) } }.text("the name of file to show")
    )

    cmd("generate-vocabulary").action((_, _) => GenerateVocabularyConfig()).children(
      arg[String]("<input>").action { (x, c) => (c: @unchecked) match { case c: GenerateVocabularyConfig =>
        c.copy(input = x) } }.text("project from which vocabulary should be generated"),
      opt[String]('o', "output").required().action { (x, c) => (c: @unchecked) match { case c: GenerateVocabularyConfig =>
        c.copy(output = x) } }.text("output file"),
      opt[Int]('s', "size").action { (x, c) => (c: @unchecked) match { case c: GenerateVocabularyConfig =>
        c.copy(vocabularySize = x) } }.text("the maximum size of the vocabulary"),
      opt[Unit]("silent").action { (_, c) => (c: @unchecked) match { case c: GenerateVocabularyConfig =>
        c.copy(silent = true) } }.text("do not output info to stdout"),
      opt[Unit]("strip-identifiers").action { (_, c) => (c: @unchecked) match { case c: GenerateVocabularyConfig =>
        c.copy(stripIdentifiers = true) } }.text("remove identifiers from output"),
      opt[Unit]("include-types").action { (_, c) => (c: @unchecked) match { case c: GenerateVocabularyConfig =>
        c.copy(includeTypes = true) } }.text("include all types in the output")
    )

    cmd("generate-skipgram-data").action((_, _) => SkipgramConfig()).children(
      arg[String]("<input>").action { (x, c) => (c: @unchecked) match { case c: SkipgramConfig =>
        c.copy(input = x) } }.text("project from which skipgram model should be trained"),
      opt[String]('v' ,"vocabulary-path").required().action { (x, c) => (c: @unchecked) match { case c: SkipgramConfig =>
        c.copy(vocabularyPath = x) } }.text("path of the saved vocabulary"),
      opt[String]('o', "output").required().action { (x, c) => (c: @unchecked) match { case c: SkipgramConfig =>
        c.copy(output = x) } }.text("output file"),
      opt[Int]("children-window-size").action { (x, c) => (c: @unchecked) match { case c: SkipgramConfig =>
        c.copy(childrenWindowSize = x) } }.text("children window size to train the model"),
      opt[Int]("ancestors-window-size").action { (x, c) => (c: @unchecked) match { case c: SkipgramConfig =>
        c.copy(ancestorsWindowSize = x) } }.text("ancestors window size to train the model"),
      opt[Unit]("without-siblings").action { (_, c) => (c: @unchecked) match { case c: SkipgramConfig =>
        c.copy(includeSiblings = false) } }.text("do not include siblings in context"),
      opt[Unit]("no-shuffle").action { (_, c) => (c: @unchecked) match { case c: SkipgramConfig =>
        c.copy(noShuffle = true) } }.text("do not shuffle the data"),
      opt[Unit]("debug").action { (_, c) => (c: @unchecked) match { case c: SkipgramConfig =>
        c.copy(debug = true) } }.text("activate debug mode")
    )

    cmd("visualize-vocabulary-distribution").action((_, _) => VisualizeVocabularyDistributionConfig()).children(
      opt[String]('v' ,"vocabulary-path").required().action { (x, c) => (c: @unchecked) match { case c: VisualizeVocabularyDistributionConfig =>
        c.copy(vocabularyPath = x) } }.text("path of the saved vocabulary"),
      opt[String]('o', "output").action { (x, c) => (c: @unchecked) match { case c: VisualizeVocabularyDistributionConfig =>
        c.copy(output = Some(x)) } }.text("output file"),
      opt[String]("title").action { (x, c) => (c: @unchecked) match { case c: VisualizeVocabularyDistributionConfig =>
        c.copy(title = x) } }.text("plot title"),
      opt[Seq[Int]]("breakpoints").action { (x, c) => (c: @unchecked) match { case c: VisualizeVocabularyDistributionConfig =>
        c.copy(breakpoints = x) } }.text("breakpoints to plot distribution"),
      opt[Unit]("no-open").action { (_, c) => (c: @unchecked) match { case c: VisualizeVocabularyDistributionConfig =>
        c.copy(openBrowser = false) } }.text("do not open the browser"),
      opt[Unit]('r', "replace").action { (_, c) => (c: @unchecked) match { case c: VisualizeVocabularyDistributionConfig =>
        c.copy(replace = true) } }.text("replace the previous file")
    )
  }

  def parse(args: Array[String]): Option[Config] = {
    // XXX: scopt does not support - as argument
    // https://github.com/scopt/scopt/issues/108
    val normalizedArgs = args.map {
      case "-" => "<STDIN>"
      case arg => arg
    }
    parser.parse(normalizedArgs, NoConfig)
  }
}
