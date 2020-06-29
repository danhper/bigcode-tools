package com.tuvistavie.bigcode.asttools.ast

import java.io._
import java.util.zip.GZIPOutputStream

import com.tuvistavie.bigcode.asttools.data._
import com.tuvistavie.bigcode.asttools.models.{Node, SkipgramConfig, Token, Vocabulary}
import com.typesafe.scalalogging.LazyLogging

class SkipgramDataGenerator(vocabulary: Vocabulary, skipgramConfig: SkipgramConfig) extends LazyLogging {
  class SkipgramQueueItemProcessor(pw: PrintWriter) extends QueueItemProcessor[String] {
    override def processItem(item: Item[String]): Unit = {
      QueueItemProcessor.stringToNode(item).content.foreach(processAst(_, pw))
    }
    override def close(): Unit = {
      pw.close()
    }
  }

  object SkipgramQueueItemProcessor extends QueueItemProcessorBuilder[String] {
    def apply(index: Int): SkipgramQueueItemProcessor = {
      val fs = new FileOutputStream(skipgramConfig.output.concat("-%03d.txt.gz".format(index)))
      val gs = new GZIPOutputStream(fs)
      val pw = new PrintWriter(gs)
      new SkipgramQueueItemProcessor(pw)
    }
  }

  def generateData(processBuilder: QueueItemProcessorBuilder[String]): Unit = {
    ASTProducerConsumerRunner.run(skipgramConfig.input, processBuilder)
  }

  def generateData(): Unit = {
    generateData(SkipgramQueueItemProcessor)
  }

  def processAst(root: Node, pw: PrintWriter): Unit = {
    VocabularyGenerator.getNodes(root).foreach { node =>
      generateContextPairs(node).foreach {
        case (word, context) if word != Vocabulary.unk && context != Vocabulary.unk =>
          pw.println(f"$word,$context")
        case (_, _) =>
      }
    }
  }

  private def generateContextPairs(node: Node): List[(Int, Int)] = {
    val context = generateContext(node)
    val nodeIndex = vocabulary.indexFor(getToken(node))
    context.map(target => nodeIndex -> vocabulary.indexFor(target))
  }

  private def generateContext(node: Node): List[Token] = {
    findAncestors(node) ++ findChildren(node) ++ maybeFindSiblings(node)
  }

  def maybeFindSiblings(node: Node): List[Token] = if (skipgramConfig.includeSiblings) {
    findSiblings(node)
  } else {
    List.empty
  }

  def findSiblings(node: Node): List[Token] = {
    node.parent.map(_.children.map(getToken)).getOrElse(List.empty)
  }

  def findChildren(node: Node, currentDepth: Int = 0): List[Token] = {
    if (currentDepth == skipgramConfig.childrenWindowSize) {
      return List.empty
    }
    node.children.map(getToken) ++ node.children.flatMap(n => findChildren(n, currentDepth + 1))
  }

  def findAncestors(node: Node, currentDepth: Int = 0): List[Token] = {
    if (currentDepth == skipgramConfig.ancestorsWindowSize) {
      return List.empty
    }
    node.parent.map(n =>
      getToken(n) +: findAncestors(n, currentDepth + 1)
    ).getOrElse(List.empty)
  }

  private def getToken(node: Node): Token = {
    node.getToken(stripIdentifiers = vocabulary.strippedIdentifiers)
  }
}

object SkipgramDataGenerator {
  def apply(vocabulary: Vocabulary, config: SkipgramConfig): SkipgramDataGenerator = {
    new SkipgramDataGenerator(vocabulary, config)
  }

  def generateData(config: SkipgramConfig): Unit = {
    val vocabulary = VocabularyGenerator.loadFromFile(config.vocabularyPath)
    val iterator = new SkipgramDataGenerator(vocabulary, config)
    iterator.generateData()
  }
}
