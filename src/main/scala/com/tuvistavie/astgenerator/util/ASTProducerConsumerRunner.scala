package com.tuvistavie.astgenerator.util

import java.util.concurrent.{CountDownLatch, Executors, LinkedBlockingQueue, TimeUnit}

import com.tuvistavie.astgenerator.data._
import com.typesafe.scalalogging.LazyLogging


object ASTProducerConsumerRunner extends LazyLogging {
  def run(filename: String, processor: Item[String] => Unit): Unit = {
    val queue = new LinkedBlockingQueue[QueueItem[String]](50)
    val producer = new StringProducer(filename, queue)
    val workers = Runtime.getRuntime.availableProcessors() - 1
    val producerThread = new Thread(producer)
    val pool = Executors.newFixedThreadPool(workers)
    val countDownLatch = new CountDownLatch(workers)
    try {
      producerThread.start()
      for (_ <- 1 to workers) {
        pool.submit(new Consumer(queue, processor, countDownLatch))
      }
      logger.info(s"starting work with $workers workers")
      producerThread.join()
      logger.info("producer thread done, waiting for workers to finish")
      (1 to workers).foreach { _ => queue.put(Stop)  }
      countDownLatch.await()
    } finally {
      pool.shutdown()
    }
  }
}
