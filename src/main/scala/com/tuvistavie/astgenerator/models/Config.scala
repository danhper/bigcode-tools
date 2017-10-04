package com.tuvistavie.astgenerator.models

import java.io.File

import scalaz.syntax.std.boolean._

sealed trait Config

case object NoConfig extends Config

case object ShowHelpConfig extends Config
case object ShowVersionConfig extends Config

case class GenerateAstConfig(
  pretty: Boolean = false,
  project: String = "",
  output: String = "",
  keepIdentifiers: Boolean = false
) extends Config

case class ExtractTokensConfig(
  project: String = ""
) extends Config

case class GenerateDotConfig(
  filepath: String = "",
  output: Option[String] = None,
  debug: Boolean = false,
  hideIdentifiers: Boolean = false,
  view: Boolean = true,
  index: Int = 0
) extends Config {
  def fileOutput: Option[String] = {
    output.orElse { view.option(temporaryPath) }
  }
  private def temporaryPath: String = {
    File.createTempFile("ast-", ".png").toString
  }
}

case class GenerateVocabularyConfig(
  project: String = "",
  output: String = "",
  subgraphDepth: Int = 1,
  vocabularySize: Int = 10000,
  silent: Boolean = false,
  stripIdentifiers: Boolean = false,
  includeTypes: Boolean = false
) extends Config

case class SkipgramConfig(
  action: String = "",
  project: String = "",
  debug: Boolean = false,
  vocabularyPath: String = "",
  output: String = "",
  epochs: Int = 5,
  childrenWindowSize: Int = 2,
  ancestorsWindowSize: Int = 2,
  includeSiblings: Boolean = true,
  embeddingSize: Int = 300,
  negativeSamples: Int = 5,
  batchSize: Int = 64,
  unigramTable: UnigramTable = null,
  vocabularySize: Int = -1,
  learningRate: Double = 0.1,
  noShuffle: Boolean = false
) extends Config

case class VisualizeEmbeddingsConfig(
  embeddingsPath: String = "",
  vocabularyPath: String = "",
  title: String = "Word embeddings",
  output: String = "tmp/embeddings.html",
  replace: Boolean = false,
  openBrowser: Boolean = true,
  dimensions: Int = 2
) extends Config

case class VisualizeVocabularyDistributionConfig(
  vocabularyPath: String = "",
  title: String = "Vocabulary distribution",
  output: String = "tmp/vocabulary-distribution.html",
  replace: Boolean = false,
  openBrowser: Boolean = true,
  breakpoints: Seq[Int] = Seq(1, 10, 100, 1000, 5000, 10000, 50000)
) extends Config

object Config {
  def version = "0.1"
  def formatVersion = "1.0"
}
