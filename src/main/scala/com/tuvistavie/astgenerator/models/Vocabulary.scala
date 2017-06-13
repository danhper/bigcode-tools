package com.tuvistavie.astgenerator.models

case class Vocabulary(
  items: Map[Int, SubgraphVocabItem],
  subgraphDepth: Int
) {
  val size: Int = items.size

  lazy val vocabHash: Map[Subgraph, Int] = items.map { case (index, item) => (item.subgraph, index) }

  def indexFor(subgraph: Subgraph): Int = {
    vocabHash.getOrElse(subgraph, Vocabulary.unk)
  }

  val totalLettersCount: Int = items.map(_._2.count).sum
}

object Vocabulary {
  val unk: Int = -1

  def apply(items: Seq[SubgraphVocabItem], subgraphDepth: Int): Vocabulary = {
    val mapItems = items.zipWithIndex.map(_.swap).toMap
    Vocabulary(mapItems, subgraphDepth)
  }
}
