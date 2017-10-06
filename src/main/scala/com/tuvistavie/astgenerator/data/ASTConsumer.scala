package com.tuvistavie.astgenerator.data

import java.util.concurrent.BlockingQueue

import com.tuvistavie.astgenerator.ast.ASTLoader
import com.tuvistavie.astgenerator.models.Subgraph
import com.typesafe.scalalogging.LazyLogging


abstract class ASTConsumer(queue: BlockingQueue[QueueItem[(Int, String)]]) extends Runnable with LazyLogging {
  def run(): Unit = {
    queue.take() match {
      case Item((index, line)) =>
        processLine(index, line)
        run()
      case Stop =>
        logger.info(s"consumer stopped")
    }
  }

  protected def processLine(index: Int, line: String): Unit = {
    ASTLoader.parseLine(index, line).foreach(processRoot)
  }

  protected def processRoot(subgraph: Subgraph)
}
