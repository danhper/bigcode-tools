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
  silent: Boolean = false
) extends Config

object Config {
  def version = "0.1"
  def formatVersion = "1.0"
}
