package com.tuvistavie.astgenerator.trainers

import com.tuvistavie.astgenerator.ast.{SkipgramIterator, VocabularyGenerator}
import com.tuvistavie.astgenerator.models.{SkipgramConfig, UnigramTable}
import com.tuvistavie.astgenerator.util.Serializer
import org.nd4j.linalg.api.ndarray.INDArray
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

  def train(skipgramIterator: SkipgramIterator): INDArray = {
    while (skipgramIterator.hasNextBash) {
      trainBatch(skipgramIterator.nextBatch(config.batchSize))
    }
    syn0
  }

  def trainBatch(samples: List[(Int, Int)]): Unit = {
    samples.foreach { case (word, context) => trainSample(word, context) }
  }

  private def trainSample(word: Int, context: Int): Unit = {
    val negativeSamples = config.unigramTable.sample(config.negativeSamples).map(_ -> 0)
    val samples = (word -> 1) +: negativeSamples
    val err = Nd4j.zeros(config.embeddingSize)

    samples.foreach { case (target, label) =>
      val wordVector = syn0(context, ->)
      val contextVector = syn1(target, ->)
      val out = Transforms.sigmoid(wordVector dot contextVector.T)
      val g = config.learningRate * (label - out(0))
      err += g
      syn1(target, ->) += syn0(context, ->) * g
    }

    syn0(context, ->) += err
  }
}

object SkipgramTrainer {
  def trainSkipgram(baseConfig: SkipgramConfig): Unit = {
    val vocabulary = VocabularyGenerator.loadFromFile(baseConfig.vocabularyPath)
    val unigram = UnigramTable.fromVocabulary(vocabulary)
    val config = baseConfig.copy(vocabularySize = vocabulary.size, unigramTable = unigram)
    val trainer = new SkipgramTrainer(config)
    val iterator = new SkipgramIterator(vocabulary, config)
    val result = trainer.train(iterator)
    Serializer.dumpINDArrayToFile(result, config.output)
  }
}
