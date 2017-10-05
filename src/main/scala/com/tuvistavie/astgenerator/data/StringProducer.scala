package com.tuvistavie.astgenerator.data

import java.util.concurrent.BlockingQueue

import com.typesafe.scalalogging.LazyLogging

import scala.io.Source

class StringProducer(val filepath: String, queue: BlockingQueue[String], progressStep: Int = 1000) extends Runnable with LazyLogging {
  // XXX: instance creation will be slow for large files
  val linesCount: Int = Source.fromFile(filepath).getLines().length

  private lazy val stream = Source.fromFile(filepath).getLines()

  private var _currentCount: Int = 0
  def currentCount: Int = _currentCount

  override def run(): Unit = {
    stream.foreach { line =>
      _currentCount += 1
      queue.put(line)
      showProgress()
    }
  }

  private def showProgress(): Unit = {
    if (progressStep > 0 && currentCount % progressStep == 0) {
      val progress = currentCount.toFloat / linesCount * 100
      logger.info(f"$currentCount / $linesCount ($progress%.2f%%)")
    }
  }
}
