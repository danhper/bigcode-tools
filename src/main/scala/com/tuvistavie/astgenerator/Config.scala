package com.tuvistavie.astgenerator

sealed trait CommandConfig

case object NoConfig extends CommandConfig

case class GenerateAstConfig(
  pretty: Boolean = false,
  project: String = "",
  output: String = "",
  keepIdentifiers: Boolean = false
) extends CommandConfig

case class Config(command: String = "", commandConfig: CommandConfig = NoConfig)

object Config {
  def version = "0.1"
  def formatVersion = "1.0"
}
