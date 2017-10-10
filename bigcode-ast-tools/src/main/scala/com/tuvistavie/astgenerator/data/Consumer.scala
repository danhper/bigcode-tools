package com.tuvistavie.astgenerator.data

import java.util.concurrent.{BlockingQueue, CountDownLatch}

import com.typesafe.scalalogging.LazyLogging


class Consumer[T](
  queue: BlockingQueue[QueueItem[T]],
  processItem: (Item[T] => Unit),
  stopLatch: CountDownLatch
) extends Runnable with LazyLogging {
  def run(): Unit = {
    var run = true
    while (run) {
      queue.take() match {
        case item: Item[T] =>
          safeProcessLine(item)
        case Stop =>
          logger.info(s"consumer stopped")
          stopLatch.countDown()
          run = false
      }
    }
  }

  private def safeProcessLine(item: Item[T]): Unit = {
    try {
      processItem(item)
    } catch {
      case e: Throwable =>
        logger.error(s"failed to process line ${item.index}: $e ${e.getStackTrace.mkString("", "\n", "\n")}")
    }
  }
}
