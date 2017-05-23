package com.tuvistavie.astgenerator

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
  depths: Seq[Int] = List(1),
  silent: Boolean = false
) extends Config

case class SkipgramConfig(
  project: String = "",
  windowDepth: Int = 2,
  includeSiblings: Boolean = true
) extends Config

object Config {
  def version = "0.1"
  def formatVersion = "1.0"
}
