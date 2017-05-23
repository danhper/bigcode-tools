package com.tuvistavie.astgenerator.models

import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4s.Implicits._


case class UnigramTable(table: INDArray)

object UnigramTable {
  def fromVocabulary(vocabulary: Vocabulary): UnigramTable = {
    UnigramTable(List(1,2).toNDArray)
  }
}
