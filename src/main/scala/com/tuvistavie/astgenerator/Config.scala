package com.tuvistavie.astgenerator

case class Config(pretty: Boolean = false,
                  project: String = "",
                  output: String = "",
                  keepIdentifiers: Boolean = false)

object Config {
  def version = "0.1"
  def formatVersion = "1.0"
}
