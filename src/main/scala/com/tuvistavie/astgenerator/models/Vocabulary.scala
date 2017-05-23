package com.tuvistavie.astgenerator.models

case class Vocabulary(items: Map[Int, SubgraphVocabItem], vocabHash: Map[Subgraph, Int]) {
  val size: Int = items.size
}
