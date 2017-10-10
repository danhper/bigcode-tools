package com.tuvistavie.astgenerator.models

import com.typesafe.scalalogging.LazyLogging
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import org.nd4s.Implicits._


case class UnigramTable(table: INDArray) {
  def sample(count: Int): List[Int] = {
    (0 until count).map(_ => table(util.Random.nextInt(table.length())).toInt).toList
  }
}

object UnigramTable extends LazyLogging {
  def fromVocabulary(vocabulary: Vocabulary, size: Int = 10 * 1000 * 1000): UnigramTable = {
    logger.debug("initializing unigram table")
    val table = Nd4j.zeros(size)
    val power = 0.75

    val normalizingConstant = vocabulary.items.values.map(v => math.pow(v.count, power)).sum

    var i = 0
    var p = 0.0

    vocabulary.items.foreach { case (key, unigram) =>
      p += math.pow(unigram.count, power) / normalizingConstant
      while (i < size && i.toFloat / size < p) {
        table(i) = key
        i += 1
      }
    }
    logger.debug("finished initializing unigram table")
    UnigramTable(table)
  }
}
