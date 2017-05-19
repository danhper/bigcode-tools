package com.tuvistavie.astgenerator.ast

import java.io.FileInputStream
import java.nio.file.{Path, Paths}

import com.github.javaparser.JavaParser
import com.github.javaparser.ast.Node
import com.tuvistavie.astgenerator.GenerateVocabularyConfig
import com.tuvistavie.astgenerator.models.Subgraph
import com.tuvistavie.astgenerator.util.{FileUtils, Serializer}

import scala.collection.JavaConverters._
import scala.collection.mutable


class VocabularyGenerator {
  def createSubgraph(node: Node, depth: Int): Subgraph = {
    if (depth == 0) {
      return Subgraph(TokenExtractor.extractToken(node))
    }
    val childSubgraphs = node.getChildNodes.asScala.map(n => createSubgraph(n, depth - 1)).toList
    val currentSubgraph = createSubgraph(node, depth - 1).copy(children = childSubgraphs)
    currentSubgraph
  }

  def generateVocabulary(filepath: String, depths: Seq[Int] = List(1)): Set[Subgraph] = {
    generateVocabulary(Paths.get(filepath), depths)
  }

  def generateVocabulary(filepath: Path, depths: Seq[Int]): Set[Subgraph] = {
    val root = JavaParser.parse(new FileInputStream(filepath.toFile))
    val vocabulary: mutable.Set[Subgraph] = mutable.Set.empty
    val nodes = getNodes(root)
    nodes.foreach { n =>
      depths.foreach { d =>
        vocabulary += createSubgraph(n, d)
      }
    }
    vocabulary.toSet
  }

  private def getNodes(root: Node): List[Node] = {
    val queue = mutable.Queue(root)
    val nodes: mutable.MutableList[Node] = mutable.MutableList.empty
    while (queue.nonEmpty) {
      val currentNode = queue.dequeue()
      nodes += currentNode
      currentNode.getChildNodes.forEach(n => queue.enqueue(n))
    }
    nodes.toList
  }
}

object VocabularyGenerator {
  def apply(): VocabularyGenerator = new VocabularyGenerator()

  def generateProjectVocabulary(config: GenerateVocabularyConfig): Set[Subgraph] = {
    val extractor = VocabularyGenerator()
    val files = FileUtils.findFiles(config.project, FileUtils.withExtension("java"))
    val vocabulary: mutable.Set[Subgraph] = mutable.Set.empty
    files.foreach { filepath =>
      vocabulary ++= extractor.generateVocabulary(filepath, config.depths)
    }
    val extractedVocabulary = vocabulary.toSet
    config.output.foreach(f => Serializer.dumpToFile(extractedVocabulary, f))
    if (!config.silent) {
      println(s"extracted ${vocabulary.size} letters from ${files.size} files")
    }
    extractedVocabulary
  }

  def loadFromFile(filepath: String): Set[Subgraph] = {
    Serializer.loadFromFile(filepath)
  }
}
