package com.tuvistavie.bigcode.asttools.data

import java.util.concurrent.{BlockingQueue, CountDownLatch}

import com.typesafe.scalalogging.LazyLogging

import scala.annotation.tailrec


class Consumer[T](
  queue: BlockingQueue[QueueItem[T]],
  processItem: (Item[T] => Unit),
  stopLatch: Option[CountDownLatch] = None
) extends Runnable with LazyLogging {
  @tailrec
  final def run(): Unit = {
    queue.take() match {
      case item: Item[T] =>
        safeProcessLine(item)
        run()
      case Stop =>
        logger.info(s"consumer stopped")
        stopLatch.foreach(_.countDown())
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
