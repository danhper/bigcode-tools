package com.tuvistavie.bigcode.asttools.ast

import com.tuvistavie.bigcode.asttools.BaseSpec
import com.tuvistavie.bigcode.asttools.models.GenerateVocabularyConfig

class VocabularyGeneratorSpec extends BaseSpec {
  val baseConfig = GenerateVocabularyConfig(input = circleFixturePath.toString)


  describe("generateVocabulary") {
    it("should populate identifiers when stripIdentifier is not passed") {
      val vocab = VocabularyGenerator(baseConfig).generateVocabulary()
      val expectedSize = 53
      vocab.size shouldEqual expectedSize
    }

    it("should not populate identifiers when stripIdentifier is passed") {
      val vocab = VocabularyGenerator(baseConfig.copy(stripIdentifiers = true)).generateVocabulary()
      val expectedSize = 21
      vocab.size shouldEqual expectedSize
      vocab.letters.foreach { token => token.value shouldEqual None }
    }
  }
}
