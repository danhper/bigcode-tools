package com.tuvistavie.astgenerator

case class Config(pretty: Boolean = false, project: String = "",
                  output: String = "")

object Config {
  def version = "1.0"
}
