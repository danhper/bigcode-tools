package com.tuvistavie.astgenerator.ast

import java.io.{FileOutputStream, PrintWriter}
import java.nio.file.{Files, Path, Paths}
import java.util.concurrent._
import java.util.concurrent.atomic.AtomicInteger

import com.tuvistavie.astgenerator.data.{ASTConsumer, QueueItem}
import com.tuvistavie.astgenerator.models._
import com.tuvistavie.astgenerator.util.{ASTProducerConsumerRunner, FileUtils}
import com.typesafe.scalalogging.LazyLogging
import resource.managed

import scala.collection.JavaConverters._
import scala.collection.mutable


class VocabularyGenerator(config: GenerateVocabularyConfig) extends LazyLogging {
  private val vocabulary: mutable.Map[Subgraph, AtomicInteger] = new ConcurrentHashMap[Subgraph, AtomicInteger]().asScala

  class VocabularyGeneratorConsumer(queue: BlockingQueue[QueueItem[(Int, String)]]) extends ASTConsumer(queue) {
    override protected def processRoot(subgraph: Subgraph): Unit = {
      generateGraphVocabulary(subgraph)
    }
  }

  def generateVocabulary(filepath: String): Unit = {
    generateVocabulary(Paths.get(filepath))
  }

  def generateVocabulary(filepath: Path): Unit = {
    FileUtils.parseFileToSubgraph(filepath).foreach(generateGraphVocabulary)
  }

  private def generateGraphVocabulary(root: Subgraph): Unit = {
    val nodes = VocabularyGenerator.getNodes(root)
    nodes.foreach { n =>
      val subgraph = VocabularyGenerator.createSubgraph(n, config.subgraphDepth, config.stripIdentifiers)
      addSubgraphToVocabulary(subgraph)
      if (!config.stripIdentifiers && config.includeTypes) {
        val typesSubgraph = VocabularyGenerator.createSubgraph(n, config.subgraphDepth, stripIdentifiers = true)
        addSubgraphToVocabulary(typesSubgraph)
      }
    }
  }

  private def addSubgraphToVocabulary(subgraph: Subgraph): Unit = {
    val letter = vocabulary.synchronized {
      vocabulary.getOrElseUpdate(subgraph, new AtomicInteger(0))
    }
    letter.getAndIncrement()
  }


  def create(size: Int): Vocabulary = {
    val items = vocabulary.toList.sortBy { case (_, count) => -count.get() }.take(size).map { case (subgraph, count) =>
      SubgraphVocabItem(subgraph, count.get())
    }
    Vocabulary(items, config.subgraphDepth, config.stripIdentifiers)
  }

  def generateVocabularyFromASTFile(): Vocabulary = {
    ASTProducerConsumerRunner.run(config.input, queue => new VocabularyGeneratorConsumer(queue))
    create(config.vocabularySize)
  }

  def generateProjectVocabulary(): Vocabulary = {
    val files = FileUtils.findFiles(config.input, FileUtils.withExtension("java"))
    val counter = new AtomicInteger()
    files.par.foreach { filepath =>
      val currentCount = counter.getAndIncrement()
      if (currentCount % 1000 == 0) {
        val progress = currentCount.toFloat / files.size * 100
        logger.info(f"$currentCount / ${files.size} ($progress%.2f%%)")
      }
      generateVocabulary(filepath)
    }
    create(config.vocabularySize)
  }
}

object VocabularyGenerator {
  def apply(config: GenerateVocabularyConfig): VocabularyGenerator = new VocabularyGenerator(config)

  def outputProjectVocabulary(config: GenerateVocabularyConfig): Vocabulary = {
    val generator = VocabularyGenerator(config)
    val vocabulary = if (config.input.endsWith(".json")) {
      generator.generateVocabularyFromASTFile()
    } else {
      generator.generateProjectVocabulary()
    }

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

  def createSubgraph(node: Subgraph, depth: Int, stripIdentifiers: Boolean = false): Subgraph = {
    if (depth == 1) {
      val token = if (stripIdentifiers) { Token(node.token.tokenType) } else { node.token }
      return Subgraph(token)
    }
    val childSubgraphs = node.children.map(n => createSubgraph(n, depth - 1))
    val currentSubgraph = createSubgraph(node, depth - 1, stripIdentifiers).copy(children = childSubgraphs)
    currentSubgraph
  }

  def getNodes(root: Subgraph): List[Subgraph] = {
    val queue = mutable.Queue(root)
    val nodes: mutable.MutableList[Subgraph] = mutable.MutableList.empty
    while (queue.nonEmpty) {
      val currentNode = queue.dequeue()
      nodes += currentNode
      currentNode.children.foreach(n => queue.enqueue(n))
    }
    nodes.toList
  }
}
