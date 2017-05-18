package com.tuvistavie.astgenerator

import com.tuvistavie.astgenerator.ast.{DotGenerator, JSONGenerator}
import com.tuvistavie.astgenerator.util.{CliParser, FileUtils}

object Main {
  def main(args: Array[String]): Unit = {
    CliParser.parse(args) match {
      case Some(config: GenerateAstConfig) =>
        JSONGenerator.run(config)
      case Some(config: ExtractTokensConfig) =>
        // extractTokens(config)
      case Some(config: GenerateDotConfig) =>
        DotGenerator.run(config)
      case Some(NoConfig) =>
        CliParser.showUsage()
      case None =>
    }
  }
}
