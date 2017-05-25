package com.tuvistavie.astgenerator.trainers

import com.tuvistavie.astgenerator.models.SkipgramConfig
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.ops.transforms.Transforms
import org.nd4s.Implicits._


class SkipgramTrainer(config: SkipgramConfig) {
  private val syn0 = {
    val dimension = Array(config.vocabularySize, config.embeddingSize)
    val min = -0.5 / config.embeddingSize.toFloat
    val max = 0.5 / config.embeddingSize.toFloat
    Nd4j.rand(dimension, min, max, Nd4j.getRandom)
  }
  private val syn1 = Nd4j.zeros(config.vocabularySize, config.embeddingSize)

  private def trainSample(word: Int, context: Int): Unit = {
    val negativeSamples = config.unigramTable.sample(config.negativeSamples).map(_ -> 0)
    val samples = (word -> 1) +: negativeSamples
    val err = Nd4j.zeros(config.embeddingSize)

    samples.foreach { case (target, label) =>
        val out = Transforms.sigmoid(syn0(context, ->) dot syn1(target, ->))
        val g = config.learningRate * (label - out(0))
        err += g
        syn1(target, ->) += syn0(context, ->) * g
    }

    syn0(context, ->) += err
  }
}
