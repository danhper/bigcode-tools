package com.tuvistavie.astgenerator

import com.tuvistavie.astgenerator.ast.{DotGenerator, JSONGenerator, VocabularyGenerator}
import com.tuvistavie.astgenerator.util.CliParser

object Main {
  def main(args: Array[String]): Unit = {
    CliParser.parse(args) match {
      case Some(config: GenerateAstConfig) =>
        JSONGenerator.run(config)
      case Some(config: ExtractTokensConfig) =>
        // extractTokens(config)
      case Some(config: GenerateDotConfig) =>
        DotGenerator.run(config)
      case Some(config: SkipgramConfig) =>
        // create embedding
      case Some(config: GenerateVocabularyConfig) =>
        VocabularyGenerator.generateProjectVocabulary(config)
      case Some(NoConfig) =>
        CliParser.showUsage()
      case None =>
    }
  }
}
