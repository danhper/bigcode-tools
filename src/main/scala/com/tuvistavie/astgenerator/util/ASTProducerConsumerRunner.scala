package com.tuvistavie.astgenerator.util

import java.util.concurrent.{BlockingQueue, Executors, LinkedBlockingQueue, TimeUnit}

import com.tuvistavie.astgenerator.data.{ASTConsumer, StringProducer}

object ASTProducerConsumerRunner {
  def run(filename: String, f: BlockingQueue[String] => ASTConsumer): Unit = {
    val queue = new LinkedBlockingQueue[String](100)
    val producer = new StringProducer(filename, queue)
    val cores = Runtime.getRuntime.availableProcessors()
    val producerThread = new Thread(producer)
    val pool = Executors.newFixedThreadPool(cores)
    try {
      producerThread.start()
      for (_ <- 1 to cores) {
        pool.submit(f(queue))
      }
      producerThread.join()
    } finally {
      pool.shutdown()
      // FIXME: 10s is very random
      pool.awaitTermination(10, TimeUnit.SECONDS)
    }
  }
}
