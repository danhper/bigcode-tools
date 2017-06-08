package com.tuvistavie.astgenerator.models

import org.nd4j.linalg.api.ndarray.INDArray
import org.scalatest.{FunSpec, Matchers}
import org.nd4s.Implicits._

class UnigramTableSpec extends FunSpec with Matchers {
  val vocabularyItems = Map(
    1 -> dummyItem(3), 2 -> dummyItem(8),
    3 -> dummyItem(1), 4 -> dummyItem(4))
  val vocabulary = Vocabulary(vocabularyItems, Map.empty, 1)

  describe("fromVocabulary") {
    it("should create a valid unigram table") {
      val unigram = UnigramTable.fromVocabulary(vocabulary, 30)
      val table = unigram.table
      table.eq(1).sum(1) should equal (arr(7))
      table.eq(2).sum(1) should equal (arr(13))
      table.eq(3).sum(1) should equal (arr(3))
      table.eq(4).sum(1) should equal (arr(7))
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
    SubgraphVocabItem(Subgraph(null), count)
  }

  private def arr(v: Int): INDArray = Array(v).asNDArray(1, 1)
}
