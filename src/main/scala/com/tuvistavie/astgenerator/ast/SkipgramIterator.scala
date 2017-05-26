package com.tuvistavie.astgenerator.ast

import java.nio.file.Path

import com.github.javaparser.ast.Node
import com.tuvistavie.astgenerator.models.{SkipgramConfig, Subgraph, Vocabulary}
import com.tuvistavie.astgenerator.util.FileUtils
import com.tuvistavie.astgenerator.util.JavaConversions._
import collection.JavaConverters._

class SkipgramIterator(vocabulary: Vocabulary, skipgramConfig: SkipgramConfig) {
  val files: Set[Path] = FileUtils.findFiles(skipgramConfig.project, FileUtils.withExtension("java"))

  def generateContext(node: Node): List[(Int, Int)] = {
    val context = findParents(node)
    val nodeIndex = vocabulary.indexFor(createSubgraph(node))
    context.map(sg => nodeIndex -> vocabulary.indexFor(sg))
  }

  def findSiblings(node: Node): List[Subgraph] = {
    node.getParentNode.toOption.map(n => n)
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
