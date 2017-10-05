package com.tuvistavie.astgenerator.data

import java.util.concurrent.{BlockingQueue, TimeUnit}

import com.tuvistavie.astgenerator.ast.ASTLoader
import com.tuvistavie.astgenerator.models.Subgraph

abstract class ASTConsumer(queue: BlockingQueue[String]) extends Runnable {
  def run(): Unit = {
    while (true) {
      val line = queue.poll(100, TimeUnit.MILLISECONDS)
      if (line != null) {
        processLine(line)
      } else {
        return
      }
    }
  }

  protected def processLine(line: String): Unit = {
    ASTLoader.parseLine(line).foreach(processRoot)
  }

  protected def processRoot(subgraph: Subgraph)
}
