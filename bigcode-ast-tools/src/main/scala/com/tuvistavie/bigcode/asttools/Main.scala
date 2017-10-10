package com.tuvistavie.bigcode.asttools

import com.tuvistavie.bigcode.asttools.ast.{AstVisualizer, SkipgramDataGenerator, VocabularyGenerator}
import com.tuvistavie.bigcode.asttools.models._
import com.tuvistavie.bigcode.asttools.util.CliParser
import com.tuvistavie.bigcode.asttools.visualizers.VocabularyDistributionVisualizer

object Main {
  def main(args: Array[String]): Unit = {
    CliParser.parse(args) match {
      case Some(config: VisualizeAstConfig) =>
        AstVisualizer.visualizeAst(config)
      case Some(config: SkipgramConfig) =>
        SkipgramDataGenerator.generateData(config)
      case Some(config: GenerateVocabularyConfig) =>
        VocabularyGenerator.outputProjectVocabulary(config)
      case Some(config: VisualizeVocabularyDistributionConfig) =>
        VocabularyDistributionVisualizer.visualizeVocabularyDistribution(config)
      case Some(ShowHelpConfig) =>
        CliParser.parser.showUsage()
      case Some(ShowVersionConfig) =>
        CliParser.parser.showHeader()
      case Some(NoConfig) =>
        CliParser.parser.showUsageAsError()
      case None =>
    }
  }
}
