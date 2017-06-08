package com.tuvistavie.astgenerator.models

case class Vocabulary(
  items: Map[Int, SubgraphVocabItem],
  vocabHash: Map[Subgraph, Int],
  subgraphDepth: Int
) {
  val size: Int = items.size

  def indexFor(subgraph: Subgraph): Int = {
    vocabHash.getOrElse(subgraph, Vocabulary.unk)
  }

  val totalLettersCount: Int = items.values.map(_.count).sum
}

object Vocabulary {
  val unk: Int = -1
}
