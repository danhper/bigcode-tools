package com.tuvistavie.astgenerator.ast

import java.io._
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger
import java.util.zip.GZIPOutputStream

import com.tuvistavie.astgenerator.models.{SkipgramConfig, Subgraph, Vocabulary}
import com.tuvistavie.astgenerator.util.FileUtils
import com.typesafe.scalalogging.LazyLogging
import resource.managed

import scala.collection.GenIterable
import scala.collection.mutable.ListBuffer
import scala.util.Random

class SkipgramIterator(vocabulary: Vocabulary, skipgramConfig: SkipgramConfig) extends LazyLogging {
  val files: Set[Path] = FileUtils.findFiles(skipgramConfig.project, FileUtils.withExtension("java"))

  private def filesList: GenIterable[Path] = {
    if (skipgramConfig.debug) {
      files
    } else if (skipgramConfig.noShuffle) {
      files.par
    } else {
      Random.shuffle(files).par
    }
  }

  private var filesIterator: Iterator[Path] = Iterator.empty
  private var currentFileNodes: Iterator[Subgraph] = Iterator.empty
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

  def nextDatum(): (Int, Int) = nextRawDatum() match {
    case (word, context) if word == Vocabulary.unk || context == Vocabulary.unk => nextDatum()
    case datum => datum
  }

  def outputData(pw: PrintWriter): Unit = {
    val totalFiles = files.size
    val doneFiles = new AtomicInteger(0)
    filesList.foreach { f =>
      val currentProgress = doneFiles.getAndIncrement()
      if (currentProgress % 1000 == 0) {
        val progress = currentProgress.toFloat / totalFiles * 100
        logger.info(f"$currentProgress / $totalFiles ($progress%.2f%%)")
      }

      if (skipgramConfig.debug) {
        pw.println(s"# file: $f")
      }

      nodesFromFile(f).par.foreach { nodes => nodes.foreach { node =>
        generateContextPairs(node).foreach {
          case (word, context) if word != Vocabulary.unk && context != Vocabulary.unk =>
            pw.println(f"$word,$context")
          case (_, _) =>
        }
      }}
    }
  }


  private def nextRawDatum(): (Int, Int) = {
    if (currentData.hasNext) {
      currentData.next()
    } else {
      currentData = generateContextPairs(nextNode()).iterator
      nextRawDatum()
    }
  }

  def nextNode(): Subgraph = {
    if (currentFileNodes.hasNext) {
      currentFileNodes.next()
    } else if (filesIterator.hasNext) {
      currentFileNodes = nodesFromNextFile().iterator
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
    filesIterator = files.iterator
    currentFileNodes = nodesFromNextFile().iterator
  }

  private def nodesFromNextFile(): List[Subgraph] = {
    nodesFromFile(filesIterator.next()).getOrElse(nodesFromNextFile())
  }

  private def nodesFromFile(filepath: Path): Option[List[Subgraph]] = {
    FileUtils.parseFileToSubgraph(filepath).map(VocabularyGenerator.getNodes)
  }

  def generateContextPairs(node: Subgraph): List[(Int, Int)] = {
    val context = generateContext(node)
    val nodeIndex = vocabulary.indexFor(createSubgraph(node))
    context.map(sg => nodeIndex -> vocabulary.indexFor(sg))
  }

  def generateContext(node: Subgraph): List[Subgraph] = {
    findAncestors(node) ++ findChildren(node) ++ maybeFindSiblings(node)
  }

  def maybeFindSiblings(node: Subgraph): List[Subgraph] = if (skipgramConfig.includeSiblings) {
    findSiblings(node)
  } else {
    List.empty
  }

  def findSiblings(node: Subgraph): List[Subgraph] = {
    node.parent.map(_.children.map(createSubgraph)).getOrElse(List.empty)
  }

  def findChildren(node: Subgraph, currentDepth: Int = 0): List[Subgraph] = {
    if (currentDepth == skipgramConfig.childrenWindowSize) {
      return List.empty
    }
    node.children.map(createSubgraph) ++ node.children.flatMap(n => findChildren(n, currentDepth + 1))
  }

  def findAncestors(node: Subgraph, currentDepth: Int = 0): List[Subgraph] = {
    if (currentDepth == skipgramConfig.ancestorsWindowSize) {
      return List.empty
    }
    node.parent.map(n =>
      createSubgraph(n) +: findAncestors(node, currentDepth + 1)
    ).getOrElse(List.empty)
  }

  private def createSubgraph(node: Subgraph): Subgraph = {
    VocabularyGenerator.createSubgraph(node, vocabulary.subgraphDepth, vocabulary.strippedIdentifiers)
  }
}

object SkipgramIterator {
  def apply(vocabulary: Vocabulary, config: SkipgramConfig): SkipgramIterator = {
    new SkipgramIterator(vocabulary, config)
  }

  def generateData(config: SkipgramConfig): Unit = {
    val vocabulary = VocabularyGenerator.loadFromFile(config.vocabularyPath)
    val iterator = new SkipgramIterator(vocabulary, config.copy(epochs = 1))
    for {
      fs <- managed(new FileOutputStream(config.output))
      gs <- managed(new GZIPOutputStream(fs))
      pw <- managed(new PrintWriter(gs))
    } {
      iterator.outputData(pw)
    }
  }
}
