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
      opt[Int]('d', "depth").valueName("<depth1>,<depth2>").action { (x, c) => (c: @unchecked) match { case c: GenerateVocabularyConfig =>
        c.copy(subgraphDepth = x) } }.text("the depth of the extracted subgraphs"),
      opt[Int]('s', "size").action { (x, c) => (c: @unchecked) match { case c: GenerateVocabularyConfig =>
        c.copy(vocabularySize = x) } }.text("the maximum size of the vocabulary"),
      opt[String]('o', "output").action { (x, c) => (c: @unchecked) match { case c: GenerateVocabularyConfig =>
        c.copy(output = Some(x)) } }.text("output file"),
      opt[Unit]("silent").action { (_, c) => (c: @unchecked) match { case c: GenerateVocabularyConfig =>
        c.copy(silent = true) } }.text("do not output info to stdout")
    )

    def skipgramChildren = List(
      arg[String]("<project>").action { (x, c) => (c: @unchecked) match { case c: SkipgramConfig =>
        c.copy(project = x) } }.text("project from which skipgram model should be trained"),
      opt[String]('v' ,"vocabulary-path").required().action { (x, c) => (c: @unchecked) match { case c: SkipgramConfig =>
        c.copy(vocabularyPath = x) } }.text("path of the saved vocabulary"),
      opt[String]('o', "output").required().action { (x, c) => (c: @unchecked) match { case c: SkipgramConfig =>
        c.copy(output = x) } }.text("output file"),
      opt[Int]("epochs").action { (x, c) => (c: @unchecked) match { case c: SkipgramConfig =>
        c.copy(epochs = x) } }.text("number of epochs to train"),
      opt[Int]("batch-size").action { (x, c) => (c: @unchecked) match { case c: SkipgramConfig =>
        c.copy(batchSize = x) } }.text("batch size"),
      opt[Double]("learning-rate").action { (x, c) => (c: @unchecked) match { case c: SkipgramConfig =>
        c.copy(learningRate = x) } }.text("learning rate"),
      opt[Int]("negative-samples").action { (x, c) => (c: @unchecked) match { case c: SkipgramConfig =>
        c.copy(negativeSamples = x) } }.text("negative samples"),
      opt[Int]("window-size").action { (x, c) => (c: @unchecked) match { case c: SkipgramConfig =>
        c.copy(windowSize = x) } }.text("window size to train the model"),
      opt[Int]("embedding-size").action { (x, c) => (c: @unchecked) match { case c: SkipgramConfig =>
        c.copy(embeddingSize = x) } }.text("size of the word embedding"),
      opt[Unit]("without-siblings").action { (_, c) => (c: @unchecked) match { case c: SkipgramConfig =>
        c.copy(includeSiblings = false) } }.text("do not include siblings in context")
    )

    cmd("generate-skipgram-data").action((_, _) => SkipgramConfig(action = "generate-data")).children(skipgramChildren: _*)
    cmd("train-skipgram").action((_, _) => SkipgramConfig(action = "train")).children(skipgramChildren: _*)

    cmd("visualize-embeddings").action((_, _) => VisualizeEmbeddingsConfig()).children(
      opt[String]('v' ,"vocabulary-path").required().action { (x, c) => (c: @unchecked) match { case c: VisualizeEmbeddingsConfig =>
        c.copy(vocabularyPath = x) } }.text("path of the saved vocabulary"),
      opt[String]('e', "embeddings-path").required().action { (x, c) => (c: @unchecked) match { case c: VisualizeEmbeddingsConfig =>
        c.copy(embeddingsPath = x) } }.text("embeddings path"),
      opt[String]('o', "output").action { (x, c) => (c: @unchecked) match { case c: VisualizeEmbeddingsConfig =>
        c.copy(output = x) } }.text("output file"),
      opt[String]("title").action { (x, c) => (c: @unchecked) match { case c: VisualizeEmbeddingsConfig =>
        c.copy(title = x) } }.text("plot title"),
      opt[Unit]("no-open").action { (_, c) => (c: @unchecked) match { case c: VisualizeEmbeddingsConfig =>
        c.copy(openBrowser = false) } }.text("do not open the browser"),
      opt[Unit]('r', "replace").action { (_, c) => (c: @unchecked) match { case c: VisualizeEmbeddingsConfig =>
        c.copy(replace = true) } }.text("replace the previous file")
    )

    cmd("visualize-vocabulary-distribution").action((_, _) => VisualizeVocabularyDistributionConfig()).children(
      opt[String]('v' ,"vocabulary-path").required().action { (x, c) => (c: @unchecked) match { case c: VisualizeVocabularyDistributionConfig =>
        c.copy(vocabularyPath = x) } }.text("path of the saved vocabulary"),
      opt[String]('o', "output").action { (x, c) => (c: @unchecked) match { case c: VisualizeVocabularyDistributionConfig =>
        c.copy(output = x) } }.text("output file"),
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
    parser.parse(args, NoConfig)
  }

  def showUsage(): Unit = parser.showUsage()
}
