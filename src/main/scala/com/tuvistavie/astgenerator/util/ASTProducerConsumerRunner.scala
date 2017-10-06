package com.tuvistavie.astgenerator.util

import java.util.concurrent.{BlockingQueue, Executors, LinkedBlockingQueue, TimeUnit}

import com.tuvistavie.astgenerator.data.{ASTConsumer, QueueItem, Stop, StringProducer}

object ASTProducerConsumerRunner {
  def run(filename: String, f: BlockingQueue[QueueItem[(Int, String)]] => ASTConsumer): Unit = {
    val queue = new LinkedBlockingQueue[QueueItem[(Int, String)]](50)
    val producer = new StringProducer(filename, queue)
    val workers = Runtime.getRuntime.availableProcessors() - 1
    val producerThread = new Thread(producer)
    val pool = Executors.newFixedThreadPool(workers)
    try {
      producerThread.start()
      for (_ <- 1 to workers) {
        pool.submit(f(queue))
      }
      producerThread.join()
      (1 to workers).foreach { _ => queue.put(Stop)  }
    } finally {
      pool.shutdown()
      // FIXME: 10s is very random
      pool.awaitTermination(10, TimeUnit.SECONDS)
    }
  }
}
