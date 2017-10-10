package com.tuvistavie.astgenerator.models

import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4s.Implicits._
import org.scalatest.{FunSpec, Matchers}

class UnigramTableSpec extends FunSpec with Matchers {
  val vocabularyItems = Seq(dummyItem(3), dummyItem(8), dummyItem(1), dummyItem(4))
  val vocabulary = Vocabulary(vocabularyItems, 1)

  describe("fromVocabulary") {
    it("should create a valid unigram table") {
      val unigram = UnigramTable.fromVocabulary(vocabulary, 30)
      val table = unigram.table
      table.eq(0).sum(1) should equal (arr(7))
      table.eq(1).sum(1) should equal (arr(13))
      table.eq(2).sum(1) should equal (arr(3))
      table.eq(3).sum(1) should equal (arr(7))
    }
  }

  describe("sample") {
    it("should return a random array with given size") {
      val unigramTable = UnigramTable.fromVocabulary(vocabulary, 30)
      val sampled = unigramTable.sample(12)
      sampled.length should equal (12)
      (0 to 4).foreach(i => {
        sampled.find(_ == i).size should not equal 12
      })
    }
  }

  private def dummyItem(count: Int): SubgraphVocabItem = {
    SubgraphVocabItem(Subgraph(Token("dummy")), count)
  }

  private def arr(v: Int): INDArray = Array(v).asNDArray(1, 1)
}
