package com.tuvistavie.astgenerator.ast

import java.nio.file.{Path, Paths}

import com.tuvistavie.astgenerator.models.{GenerateVocabularyConfig, SkipgramConfig, Vocabulary}
import org.scalatest.{FunSpec, Matchers}

class SkipgramIteratorSpec extends FunSpec with Matchers {
  val root: Path = Paths.get(getClass.getResource("/fixtures/dummy_project/src/main").getPath)
  val baseConfig: SkipgramConfig = SkipgramConfig(input = root.toString)

  val vocabularyConfig: GenerateVocabularyConfig = GenerateVocabularyConfig(input = root.toString, silent = true)
  val vocabulary: Vocabulary = VocabularyGenerator(vocabularyConfig).generateProjectVocabulary()

  val someBigNumber: Int = 1000000

  describe("nextBatch") {
    it("should return the correct number of samples") {
      val it = makeIterator()
      it.nextBatch(10).size should equal (10)
      it.nextBatch(50).size should equal (50)
    }

    it("should respect the number of epochs") {
      val oneEpochData = makeIterator(baseConfig.copy(epochs = 1)).nextBatch(someBigNumber)
      val twoEpochsData = makeIterator(baseConfig.copy(epochs = 2)).nextBatch(someBigNumber)
      twoEpochsData.size should equal(oneEpochData.size * 2)
    }

    it("should have more data when window size increases") {
      val sizeOneWindowData = makeIterator(baseConfig.copy(childrenWindowSize = 1)).nextBatch(someBigNumber)
      val sizeTwoWindowData = makeIterator(baseConfig.copy(childrenWindowSize = 2)).nextBatch(someBigNumber)
      sizeTwoWindowData.size should be > sizeOneWindowData.size
    }

    it("should have more data when siblings are included") {
      val dataWithSiblings = makeIterator(baseConfig.copy(includeSiblings = true)).nextBatch(someBigNumber)
      val dataWithoutSiblings = makeIterator(baseConfig.copy(includeSiblings = false)).nextBatch(someBigNumber)
      dataWithSiblings.size should be > dataWithoutSiblings.size
    }
  }

  def makeIterator(config: SkipgramConfig = baseConfig): SkipgramIterator = {
    SkipgramIterator(vocabulary, config)
  }
}
