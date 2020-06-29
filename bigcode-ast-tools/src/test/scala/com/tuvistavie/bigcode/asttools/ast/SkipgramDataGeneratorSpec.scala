package com.tuvistavie.bigcode.asttools.ast

import java.io.{ByteArrayOutputStream, PrintWriter}

import com.tuvistavie.bigcode.asttools.BaseSpec
import com.tuvistavie.bigcode.asttools.data.{QueueItemProcessor, QueueItemProcessorBuilder}
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
      sizeTwoWindowData.toSet.size should be > sizeOneWindowData.toSet.size
    }

    it("should have more data when children window size increases") {
      val sizeOneWindowData = runSkipgramGenerator(baseConfig.copy(childrenWindowSize = 1))
      val sizeTwoWindowData = runSkipgramGenerator(baseConfig.copy(childrenWindowSize = 2))
      sizeTwoWindowData.toSet.size should be > sizeOneWindowData.toSet.size
    }

    it("should have more data when siblings are included") {
      val dataWithSiblings = runSkipgramGenerator(baseConfig.copy(includeSiblings = true))
      val dataWithoutSiblings = runSkipgramGenerator(baseConfig.copy(includeSiblings = false))
      dataWithSiblings.toSet.size should be > dataWithoutSiblings.toSet.size
    }
  }

  def runSkipgramGenerator(config: SkipgramConfig): List[String] = {
    val generator = SkipgramDataGenerator(vocabulary, config)
    val outputStream = new ByteArrayOutputStream()
    for (writer <- managed(new PrintWriter(outputStream))) {
      // FIXME: this looks horribe
      val dummyBuilder = new QueueItemProcessorBuilder[String] {
        override def apply(index: Int): QueueItemProcessor[String] = new generator.SkipgramQueueItemProcessor(writer) {
          override def close(): Unit = ()
        }
      }
      generator.generateData(dummyBuilder)
    }
    outputStream.toString.split("\n").toList
  }
}
