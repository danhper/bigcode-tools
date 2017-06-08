package com.tuvistavie.astgenerator.ast

import java.io.FileInputStream
import java.nio.file.{Path, Paths}

import com.github.javaparser.JavaParser
import com.github.javaparser.ast.Node
import com.tuvistavie.astgenerator.models.{GenerateVocabularyConfig, Subgraph, SubgraphVocabItem, Vocabulary}
import com.tuvistavie.astgenerator.util.{FileUtils, Serializer}

import scala.collection.JavaConverters._
import scala.collection.mutable


class VocabularyGenerator(subgraphDepth: Int) {
  private val vocabulary: mutable.Map[Subgraph, Int] = mutable.Map.empty
  private val vocabularyItems: mutable.Map[Int, SubgraphVocabItem] = mutable.Map.empty

  def generateVocabulary(filepath: String): Unit = {
    generateVocabulary(Paths.get(filepath))
  }

  def generateVocabulary(filepath: Path): Unit = {
    val root = JavaParser.parse(new FileInputStream(filepath.toFile))
    val nodes = VocabularyGenerator.getNodes(root)
    nodes.foreach { n =>
      val subgraph = VocabularyGenerator.createSubgraph(n, subgraphDepth)
      if (!vocabulary.contains(subgraph)) {
        vocabulary += (subgraph -> vocabularyItems.size)
        vocabularyItems += (vocabularyItems.size -> SubgraphVocabItem(subgraph))
      }
      val index = vocabulary(subgraph)
      val item = vocabularyItems(index)
      vocabularyItems.update(index, item.copy(count = item.count + 1))
    }
  }


  def create(): Vocabulary = {
    Vocabulary(vocabularyItems.toMap, vocabulary.toMap, subgraphDepth)
  }
}

object VocabularyGenerator {
  def apply(subgraphDepth: Int): VocabularyGenerator = new VocabularyGenerator(subgraphDepth)

  def generateProjectVocabulary(config: GenerateVocabularyConfig): Vocabulary = {
    val generator = VocabularyGenerator(config.subgraphDepth)
    val files = FileUtils.findFiles(config.project, FileUtils.withExtension("java"))
    files.foreach { filepath =>
      generator.generateVocabulary(filepath)
    }
    val extractedVocabulary = generator.create()
    config.output.foreach(f => Serializer.dumpToFile(extractedVocabulary, f))
    if (!config.silent) {
      println(s"extracted ${extractedVocabulary.size} letters from ${files.size} files")
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
}
