package com.tuvistavie.bigcode.asttools.ast

import java.io.{ByteArrayOutputStream, PrintWriter}

import com.tuvistavie.bigcode.asttools.BaseSpec
import com.tuvistavie.bigcode.asttools.models.{GenerateVocabularyConfig, SkipgramConfig, Vocabulary}
import resource.managed

class SkipgramDataGeneratorSpec extends BaseSpec {
  val baseConfig: SkipgramConfig = SkipgramConfig(input = circleFixturePath.toString)

  val vocabulary: Vocabulary = {
    val vocabularyConfig: GenerateVocabularyConfig = GenerateVocabularyConfig(input = circleFixturePath.toString, silent = true)
    val generator = VocabularyGenerator(vocabularyConfig)
    generator.generateVocabulary()
    generator.create(1000)
  }

  describe("generateData") {
    it("should have more data when ancestors window size increases") {
      val sizeOneWindowData = runSkipgramGenerator(baseConfig.copy(ancestorsWindowSize = 1))
      val sizeTwoWindowData = runSkipgramGenerator(baseConfig.copy(ancestorsWindowSize = 2))
      sizeTwoWindowData.size should be > sizeOneWindowData.size
    }

    it("should have more data when children window size increases") {
      val sizeOneWindowData = runSkipgramGenerator(baseConfig.copy(childrenWindowSize = 1))
      val sizeTwoWindowData = runSkipgramGenerator(baseConfig.copy(childrenWindowSize = 2))
      sizeTwoWindowData.size should be > sizeOneWindowData.size
    }

    it("should have more data when siblings are included") {
      val dataWithSiblings = runSkipgramGenerator(baseConfig.copy(includeSiblings = true))
      val dataWithoutSiblings = runSkipgramGenerator(baseConfig.copy(includeSiblings = false))
      dataWithSiblings.size should be > dataWithoutSiblings.size
    }
  }

  def runSkipgramGenerator(config: SkipgramConfig): List[String] = {
    val generator = SkipgramDataGenerator(vocabulary, config)
    val outputStream = new ByteArrayOutputStream()
    for (writer <- managed(new PrintWriter(outputStream))) {
      generator.generateData(writer)
    }
    outputStream.toString.split("\n").toList
  }
}
