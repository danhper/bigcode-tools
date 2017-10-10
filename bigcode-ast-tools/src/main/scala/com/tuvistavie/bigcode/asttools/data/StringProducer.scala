package com.tuvistavie.bigcode.asttools.data

import java.nio.charset.CodingErrorAction
import java.util.concurrent.BlockingQueue

import com.typesafe.scalalogging.LazyLogging

import scala.io.{Codec, Source}

class StringProducer(val filepath: String, queue: BlockingQueue[QueueItem[String]], progressStep: Int = 1000) extends Runnable with LazyLogging {
  // NOTE: js150 is not valid utf-8
  private implicit val codec: Codec = Codec("UTF-8")
  codec.onMalformedInput(CodingErrorAction.REPLACE)
  codec.onUnmappableCharacter(CodingErrorAction.REPLACE)

  // XXX: instance creation will be slow for large files
  val linesCount: Int = {
    logger.info(s"loading $filepath ...")
    Source.fromFile(filepath).getLines().length
  }

  private val lines = Source.fromFile(filepath).getLines()


  private var _currentCount: Int = 0
  def currentCount: Int = _currentCount

  override def run(): Unit = {
    lines.zipWithIndex.foreach { case (line, index) =>
      _currentCount += 1
      queue.put(Item(index, line))
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
