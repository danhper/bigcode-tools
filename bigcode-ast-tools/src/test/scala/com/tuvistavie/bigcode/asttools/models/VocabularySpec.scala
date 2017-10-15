package com.tuvistavie.bigcode.asttools.models

import org.scalatest.{FunSpec, Matchers}

class VocabularySpec extends FunSpec with Matchers {
  val vocabularyItems: Seq[VocabItem] = Seq(
    VocabItem(Token("FooStmt"), 1),
    VocabItem(Token("BarExpr", Some("barValue")), 3),
    VocabItem(Token("Baz", Some("")), 2)
  )

  val vocabularyWithIdentifiers: Vocabulary = Vocabulary(vocabularyItems)

  val vocabTSVWithIdentifiers: String = s"""|id\ttype\tmetaType\tcount\tvalue
                                    |0\tFooStmt\tStmt\t1\t
                                    |1\tBarExpr\tExpr\t3\t"barValue"
                                    |2\tBaz\tOther\t2\t""""".stripMargin
  val vocabTSVWithoutIdentifiers: String = s"""|id\ttype\tmetaType\tcount
                                               |0\tFooStmt\tStmt\t1
                                               |1\tBarExpr\tExpr\t3
                                               |2\tBaz\tOther\t2""".stripMargin

  describe("toTSV") {
    it("should output correct TSV with identifiers") {
      vocabularyWithIdentifiers.toTSV shouldEqual vocabTSVWithIdentifiers
    }

    it("should output correct TSV without identifiers") {
      val vocabularyWithoutIdentifiers: Vocabulary = Vocabulary(vocabularyItems, strippedIdentifiers = true)
      vocabularyWithoutIdentifiers.toTSV shouldEqual vocabTSVWithoutIdentifiers
    }
  }

  describe("fromTSV") {
    it("should parse TSV with identifiers") {
      Vocabulary.fromTSV(vocabTSVWithIdentifiers) shouldEqual vocabularyWithIdentifiers
    }

    it("should parse TSV without identifiers") {
      val barExprWithoutTokenVocabItem = VocabItem(Token("BarExpr"), 3)
      val bazWithoutToken = VocabItem(Token("Baz"), 2)
      val vocabularyWithoutIdentifiers: Vocabulary = Vocabulary(
        Seq(vocabularyItems.head, barExprWithoutTokenVocabItem, bazWithoutToken), strippedIdentifiers = true)
      Vocabulary.fromTSV(vocabTSVWithoutIdentifiers) shouldEqual vocabularyWithoutIdentifiers
    }
  }
}
