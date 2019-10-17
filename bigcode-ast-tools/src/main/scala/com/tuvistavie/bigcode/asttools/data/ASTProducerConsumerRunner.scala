package com.tuvistavie.bigcode.asttools.data

import java.util.concurrent.{CountDownLatch, Executors, LinkedBlockingQueue}

import com.typesafe.scalalogging.LazyLogging


object ASTProducerConsumerRunner extends LazyLogging {
  def run(filename: String, processor: QueueItemProcessorBuilder[String]): Unit = {
    val queue = new LinkedBlockingQueue[QueueItem[String]](50)
    val producer = new StringProducer(filename, queue)
    val availableProcessors = Runtime.getRuntime.availableProcessors()
    val workers = if (availableProcessors <= 1) 1 else availableProcessors - 1
    val producerThread = new Thread(producer)
    val pool = Executors.newFixedThreadPool(workers)
    val countDownLatch = new CountDownLatch(workers)
    try {
      producerThread.start()
      for (i <- 1 to workers) {
        pool.submit(new Consumer(queue, processor(i), Some(countDownLatch)))
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
