package com.tuvistavie.bigcode.asttools.ast

import java.io.{FileOutputStream, PrintWriter}
import java.nio.file.{Files, Paths}
import java.util.concurrent._
import java.util.concurrent.atomic.AtomicInteger

import com.tuvistavie.bigcode.asttools.data.{ASTProducerConsumerRunner, Item, QueueItemProcessor}
import com.tuvistavie.bigcode.asttools.models._
import com.typesafe.scalalogging.LazyLogging
import resource.managed

import scala.collection.JavaConverters._
import scala.collection.mutable


class VocabularyGenerator(config: GenerateVocabularyConfig) extends LazyLogging {
  private val vocabulary: mutable.Map[Token, AtomicInteger] = new ConcurrentHashMap[Token, AtomicInteger]().asScala

  def generateVocabulary(): Vocabulary = {
    val processor = QueueItemProcessor.stringToNode _ andThen processNodeItem
    ASTProducerConsumerRunner.run(config.input, processor)
    create(config.vocabularySize)
  }

  def generateGraphVocabulary(root: Node): Unit = {
    val nodes = VocabularyGenerator.getNodes(root)
    nodes.foreach { node =>
      addTokenToVocabulary(node.getToken(stripIdentifiers = config.stripIdentifiers))
      if (!config.stripIdentifiers && config.includeTypes) {
        addTokenToVocabulary(node.getToken(stripIdentifiers = true))
      }
    }
  }

  private def processNodeItem(item: Item[Option[Node]]): Unit = {
    item.content.foreach(generateGraphVocabulary)
  }

  private def addTokenToVocabulary(token: Token): Unit = {
    val letter = vocabulary.synchronized {
      vocabulary.getOrElseUpdate(token, new AtomicInteger(0))
    }
    letter.getAndIncrement()
  }


  def create(size: Int): Vocabulary = {
    val items = vocabulary.toList.sortBy { case (_, count) => -count.get() }.take(size).map { case (token, count) =>
      VocabItem(token, count.get())
    }
    Vocabulary(items, config.stripIdentifiers)
  }
}

object VocabularyGenerator {
  def apply(config: GenerateVocabularyConfig): VocabularyGenerator = new VocabularyGenerator(config)

  def outputProjectVocabulary(config: GenerateVocabularyConfig): Vocabulary = {
    val generator = VocabularyGenerator(config)
    val vocabulary = generator.generateVocabulary()

    for {
      fs <- managed(new FileOutputStream(config.output))
      pw <- managed(new PrintWriter(fs))
    } {
      pw.println(vocabulary.toTSV)
    }

    if (!config.silent) {
      println(s"extracted ${vocabulary.size} letters")
    }
    vocabulary
  }

  def loadFromFile(filepath: String): Vocabulary = {
    val fileContent = new String(Files.readAllBytes(Paths.get(filepath)))
    Vocabulary.fromTSV(fileContent)
  }

  def getNodes(root: Node): List[Node] = {
    val queue = mutable.Queue(root)
    val nodes: mutable.MutableList[Node] = mutable.MutableList.empty
    while (queue.nonEmpty) {
      val currentNode = queue.dequeue()
      nodes += currentNode
      currentNode.children.foreach(n => queue.enqueue(n))
    }
    nodes.toList
  }
}
