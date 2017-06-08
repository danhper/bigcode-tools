package com.tuvistavie.astgenerator.models

sealed trait Config

case object NoConfig extends Config

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
  silent: Boolean = false,
  hideIdentifiers: Boolean = false
) extends Config

case class GenerateVocabularyConfig(
  project: String = "",
  output: Option[String] = None,
  subgraphDepth: Int = 1,
  silent: Boolean = false
) extends Config

case class SkipgramConfig(
  project: String = "",
  vocabularyPath: String = "",
  output: String = "",
  epochs: Int = 5,
  windowSize: Int = 2,
  includeSiblings: Boolean = true,
  embeddingSize: Int = 300,
  negativeSamples: Int = 5,
  batchSize: Int = 64,
  unigramTable: UnigramTable = null,
  vocabularySize: Int = -1,
  learningRate: Double = 0.5
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

object Config {
  def version = "0.1"
  def formatVersion = "1.0"
}
