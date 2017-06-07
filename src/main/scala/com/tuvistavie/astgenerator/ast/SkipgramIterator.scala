package com.tuvistavie.astgenerator.ast

import java.io.FileInputStream
import java.nio.file.Path

import com.github.javaparser.JavaParser
import com.github.javaparser.ast.Node
import com.tuvistavie.astgenerator.models.{SkipgramConfig, Subgraph, Vocabulary}
import com.tuvistavie.astgenerator.util.FileUtils
import com.tuvistavie.astgenerator.util.JavaConversions._
import com.typesafe.scalalogging.LazyLogging

import collection.JavaConverters._
import scala.collection.mutable.ListBuffer

class SkipgramIterator(vocabulary: Vocabulary, skipgramConfig: SkipgramConfig) extends LazyLogging {
  val files: Set[Path] = FileUtils.findFiles(skipgramConfig.project, FileUtils.withExtension("java"))

  private var filesIterator: Iterator[Path] = Iterator.empty
  private var currentFileNodes: Iterator[Node] = Iterator.empty
  private var currentData: Iterator[(Int, Int)] = Iterator.empty
  private var currentEpoch: Int = 0

  def nextBatch(size: Int): List[(Int, Int)] = {
    val result = ListBuffer.empty[(Int, Int)]
    while (result.length < size && hasMoreData) {
      result.append(nextDatum())
    }

    result.toList
  }

  def hasNextBash: Boolean = hasMoreData

  def hasMoreData: Boolean = {
    currentData.hasNext || currentFileNodes.hasNext || filesIterator.hasNext || currentEpoch < skipgramConfig.epochs
  }

  def nextDatum(): (Int, Int) = {
    if (currentData.hasNext) {
      currentData.next()
    } else {
      currentData = generateContextPairs(nextNode()).iterator
      nextDatum()
    }
  }

  def nextNode(): Node = {
    if (currentFileNodes.hasNext) {
      currentFileNodes.next()
    } else if (filesIterator.hasNext) {
      currentFileNodes = nodesFromFile(filesIterator.next()).iterator
      nextNode()
    } else if (currentEpoch < skipgramConfig.epochs) {
      moveToNextEpoch()
      nextNode()
    } else {
      throw new RuntimeException("no next node")
    }
  }

  private def moveToNextEpoch(): Unit = {
    currentEpoch += 1
    logger.debug(s"iterating on epoch $currentEpoch")
    filesIterator = files.iterator
    currentFileNodes = nodesFromFile(filesIterator.next()).iterator
  }

  private def nodesFromFile(filepath: Path): List[Node] = {
    logger.debug(s"iterating on file $filepath")
    val root = JavaParser.parse(new FileInputStream(filepath.toFile))
    VocabularyGenerator.getNodes(root)
  }

  def generateContextPairs(node: Node): List[(Int, Int)] = {
    val context = generateContext(node)
    val nodeIndex = vocabulary.indexFor(createSubgraph(node))
    context.map(sg => nodeIndex -> vocabulary.indexFor(sg))
  }

  def generateContext(node: Node): List[Subgraph] = {
    findParents(node) ++ findChildren(node) ++ maybeFindSiblings(node)
  }

  def maybeFindSiblings(node: Node): List[Subgraph] = if (skipgramConfig.includeSiblings) {
    findSiblings(node)
  } else {
    List.empty
  }

  def findSiblings(node: Node): List[Subgraph] = {
    val makeSubgraphs = (n: Node)=> n.getChildNodes.asScala.map(createSubgraph).toList
    node.getParentNode.toOption.map(makeSubgraphs).getOrElse(List.empty)
  }

  def findChildren(node: Node, currentDepth: Int = 0): List[Subgraph] = {
    if (currentDepth == skipgramConfig.windowSize) {
      return List.empty
    }
    val children = node.getChildNodes.asScala.toList
    children.map(createSubgraph) ++ children.flatMap(n => findChildren(n, currentDepth + 1))
  }

  def findParents(node: Node, currentDepth: Int = 0): List[Subgraph] = {
    if (currentDepth == skipgramConfig.windowSize) {
      return List.empty
    }
    node.getParentNode.toOption.map(n =>
      createSubgraph(n) +: findParents(node, currentDepth + 1)
    ).getOrElse(List.empty)
  }

  private def createSubgraph(node: Node): Subgraph = {
    VocabularyGenerator.createSubgraph(node, vocabulary.subgraphDepth)
  }
}

object SkipgramIterator {
  def apply(vocabulary: Vocabulary, config: SkipgramConfig): SkipgramIterator = {
    new SkipgramIterator(vocabulary, config)
  }
}
