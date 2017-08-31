package com.tuvistavie.astgenerator.ast

import java.io.{FileOutputStream, PrintWriter}
import java.nio.file.{Path, Paths}
import java.util.concurrent.ConcurrentHashMap

import com.github.javaparser.ast.{CompilationUnit, Node}
import com.tuvistavie.astgenerator.models._
import com.tuvistavie.astgenerator.util.{FileUtils, Serializer}
import com.typesafe.scalalogging.LazyLogging
import resource.managed

import scala.collection.JavaConverters._
import scala.collection.mutable


class VocabularyGenerator(subgraphDepth: Int) extends LazyLogging {
  private val vocabulary: mutable.Map[Subgraph, Int] = new ConcurrentHashMap[Subgraph, Int]().asScala
  private val vocabularyItems: mutable.Map[Int, SubgraphVocabItem] = new ConcurrentHashMap[Int, SubgraphVocabItem]().asScala

  def generateVocabulary(filepath: String): Unit = {
    generateVocabulary(Paths.get(filepath))
  }

  private def generateVocabulary(cu: CompilationUnit): Unit = {
    val nodes = VocabularyGenerator.getNodes(cu)
    nodes.foreach { n =>
      val subgraph = VocabularyGenerator.createSubgraph(n, subgraphDepth)
      synchronized {
        if (!vocabulary.contains(subgraph)) {
          vocabulary += (subgraph -> vocabularyItems.size)
          vocabularyItems += (vocabularyItems.size -> SubgraphVocabItem(subgraph))
        }
      }
      val index = vocabulary(subgraph)
      val item = vocabularyItems(index)
      item.currentCount.getAndIncrement()
    }
  }

  def generateVocabulary(filepath: Path): Unit = {
    FileUtils.parseFile(filepath).foreach(generateVocabulary)
  }

  def create(size: Int): Vocabulary = {
    val items = vocabularyItems.values.toSeq.sortBy(-_.count).take(size)
    Vocabulary(items, subgraphDepth)
  }

  def generateProjectVocabulary(projectPath: String, vocabularySize: Int): Vocabulary = {
    val files = FileUtils.findFiles(projectPath, FileUtils.withExtension("java"))
    files.par.foreach { filepath =>
      generateVocabulary(filepath)
    }
    create(vocabularySize)
  }
}

object VocabularyGenerator {
  def apply(subgraphDepth: Int): VocabularyGenerator = new VocabularyGenerator(subgraphDepth)

  def generateProjectVocabulary(config: GenerateVocabularyConfig): Vocabulary = {
    val generator = VocabularyGenerator(config.subgraphDepth)
    val extractedVocabulary = generator.generateProjectVocabulary(config.project, config.vocabularySize)
    config.output.foreach(f => Serializer.dumpToFile(extractedVocabulary, f))
    if (!config.silent) {
      println(s"extracted ${extractedVocabulary.size} letters")
    }
    extractedVocabulary
  }

  def loadFromFile(filepath: String): Vocabulary = {
    Serializer.loadFromFile[Vocabulary](filepath)
  }

  def createSubgraph(node: Node, depth: Int): Subgraph = {
    if (depth == 1) {
      return Subgraph(TokenExtractor.extractToken(node))
    }
    val childSubgraphs = node.getChildNodes.asScala.map(n => createSubgraph(n, depth - 1)).toList
    val currentSubgraph = createSubgraph(node, depth - 1).copy(children = childSubgraphs)
    currentSubgraph
  }

  def getNodes(root: Node): List[Node] = {
    val queue = mutable.Queue(root)
    val nodes: mutable.MutableList[Node] = mutable.MutableList.empty
    while (queue.nonEmpty) {
      val currentNode = queue.dequeue()
      nodes += currentNode
      currentNode.getChildNodes.asScala.foreach(n => queue.enqueue(n))
    }
    nodes.toList
  }

  def createVocabularyLabels(config: CreateVocabularyLabelsConfig): Unit = {
    val vocabulary = loadFromFile(config.vocabularyPath)
    for {
      fs <- managed(new FileOutputStream(config.output))
      pw <- managed(new PrintWriter(fs))
    } {
      vocabulary.items.values.foreach(vocabItem => pw.println(vocabItem.subgraph.toString))
    }
  }
}
