package com.tuvistavie.astgenerator

import com.tuvistavie.astgenerator.ast._
import com.tuvistavie.astgenerator.models._
import com.tuvistavie.astgenerator.trainers.SkipgramTrainer
import com.tuvistavie.astgenerator.util.CliParser
import com.tuvistavie.astgenerator.visualizers.{EmbeddingVisualizer, VocabularyDistributionVisualizer}

object Main {
  def main(args: Array[String]): Unit = {
    CliParser.parse(args) match {
      case Some(config: GenerateAstConfig) =>
        JSONGenerator.run(config)
      case Some(config: ExtractTokensConfig) =>
        // extractTokens(config)
      case Some(config: GenerateDotConfig) =>
        DotGenerator.generateDot(config)
      case Some(config: SkipgramConfig) => config.action match {
        case "train" => SkipgramTrainer.trainSkipgram(config)
        case "generate-data" => SkipgramIterator.generateData(config)
      }
      case Some(config: GenerateVocabularyConfig) =>
        VocabularyGenerator.outputProjectVocabulary(config)
      case Some(config: VisualizeVocabularyDistributionConfig) =>
        VocabularyDistributionVisualizer.visualizeVocabularyDistribution(config)
      case Some(config: VisualizeEmbeddingsConfig) =>
        EmbeddingVisualizer.visualizeEmbeddings(config)
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
