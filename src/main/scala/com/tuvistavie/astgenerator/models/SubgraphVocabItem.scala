package com.tuvistavie.astgenerator.models

import java.util.concurrent.atomic.AtomicInteger

case class SubgraphVocabItem(subgraph: Subgraph, currentCount: AtomicInteger = new AtomicInteger(0)) {
  def count: Int = currentCount.get()
}
