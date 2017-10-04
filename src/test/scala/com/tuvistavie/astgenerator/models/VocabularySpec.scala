package com.tuvistavie.astgenerator.models

import org.scalatest.{FunSpec, Matchers}

class VocabularySpec extends FunSpec with Matchers {
  val vocabularyItems: Seq[SubgraphVocabItem] = Seq(
    SubgraphVocabItem(Subgraph(Token("FooStmt")), 1),
    SubgraphVocabItem(Subgraph(Token("BarExpr", Some("barValue"))), 3),
    SubgraphVocabItem(Subgraph(Token("Baz")), 2))

  val vocabularyWithIdentifiers: Vocabulary = Vocabulary(vocabularyItems, 1)

  val vocabTSVWithIdentifiers: String = s"""|id\ttype\tmetaType\tcount\tvalue
                                    |0\tFooStmt\tStmt\t1\t
                                    |1\tBarExpr\tExpr\t3\tbarValue
                                    |2\tBaz\tOther\t2\t""".stripMargin
  val vocabTSVWithoutIdentifiers: String = s"""|id\ttype\tmetaType\tcount
                                               |0\tFooStmt\tStmt\t1
                                               |1\tBarExpr\tExpr\t3
                                               |2\tBaz\tOther\t2""".stripMargin

  describe("toTSV") {
    it("should output correct TSV with identifiers") {
      vocabularyWithIdentifiers.toTSV should equal(vocabTSVWithIdentifiers)
    }

    it("should output correct TSV without identifiers") {
      val vocabularyWithoutIdentifiers: Vocabulary = Vocabulary(vocabularyItems, 1, strippedIdentifiers = true)
      vocabularyWithoutIdentifiers.toTSV should equal(vocabTSVWithoutIdentifiers)
    }
  }

  describe("fromTSV") {
    it("should parse TSV with identifiers") {
      Vocabulary.fromTSV(vocabTSVWithIdentifiers) should equal(vocabularyWithIdentifiers)
    }

    it("should parse TSV without identifiers") {
      val barExprWithoutTokenSubgraphVocabItem = SubgraphVocabItem(Subgraph(Token("BarExpr")), 3)
      val vocabularyWithoutIdentifiers: Vocabulary = Vocabulary(
        Seq(vocabularyItems.head, barExprWithoutTokenSubgraphVocabItem, vocabularyItems.last), 1, strippedIdentifiers = true)
      Vocabulary.fromTSV(vocabTSVWithoutIdentifiers) should equal(vocabularyWithoutIdentifiers)
    }
  }
}
