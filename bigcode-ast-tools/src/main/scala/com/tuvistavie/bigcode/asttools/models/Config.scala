package com.tuvistavie.bigcode.asttools.models

import java.io.File

import scalaz.syntax.std.boolean._

sealed trait Config

case object NoConfig extends Config

case object ShowHelpConfig extends Config
case object ShowVersionConfig extends Config


case class VisualizeAstConfig(
  input: String = "",
  output: Option[String] = None,
  debug: Boolean = false,
  hideIdentifiers: Boolean = false,
  view: Boolean = true,
  index: Option[Int] = None,
  filesListPath: Option[String] = None,
  filename: Option[String] = None
) extends Config {
  def fileOutput: Option[String] = {
    output.orElse { view.option(temporaryPath) }
  }
  private def temporaryPath: String = {
    File.createTempFile("ast-", ".png").toString
  }
}

case class GenerateVocabularyConfig(
  input: String = "",
  output: String = "",
  vocabularySize: Int = 10000,
  silent: Boolean = false,
  stripIdentifiers: Boolean = false,
  includeTypes: Boolean = false
) extends Config

case class SkipgramConfig(
  input: String = "",
  debug: Boolean = false,
  vocabularyPath: String = "",
  output: String = "",
  childrenWindowSize: Int = 2,
  ancestorsWindowSize: Int = 2,
  includeSiblings: Boolean = true,
  vocabularySize: Int = -1,
  noShuffle: Boolean = false
) extends Config

case class VisualizeVocabularyDistributionConfig(
  vocabularyPath: String = "",
  title: String = "Vocabulary distribution",
  output: Option[String] = None,
  replace: Boolean = false,
  openBrowser: Boolean = true,
  breakpoints: Seq[Int] = Seq(1, 10, 100, 1000, 5000, 10000, 50000)
) extends Config {
  def fileOutput: String = {
    output.getOrElse(File.createTempFile("bigcode-vocab-", ".html").toString)
  }
}

object Config {
  def version = "0.1"
  def formatVersion = "1.0"
}
