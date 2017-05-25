package com.tuvistavie.astgenerator.models

import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import org.nd4s.Implicits._


case class UnigramTable(table: INDArray) {
  def sample(count: Int): List[Int] = {
    (0 until count).map(_ => util.Random.nextInt(table.length())).toList
  }
}

object UnigramTable {
  def fromVocabulary(vocabulary: Vocabulary, size: Int = 10 * 1000 * 1000): UnigramTable = {
    val table = Nd4j.zeros(size)
    val power = 0.75

    val normalizingConstant = vocabulary.items.map { case (_, v) => math.pow(v.count, power) }.sum

    var i = 0
    var p = 0.0

    vocabulary.items.foreach { case (key, unigram) =>
      p += math.pow(unigram.count, power) / normalizingConstant
      while (i < size && i.toFloat / size < p) {
        table(i) = key
        i += 1
      }
    }
    UnigramTable(table)
  }
}
