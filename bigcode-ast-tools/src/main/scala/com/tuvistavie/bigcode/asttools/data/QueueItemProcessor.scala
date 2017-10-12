package com.tuvistavie.bigcode.asttools.data

import com.tuvistavie.bigcode.asttools.models.Node
import com.tuvistavie.bigcode.asttools.ast.AstLoader

trait QueueItemProcessor[T] extends AutoCloseable {
  def processItem(item: Item[T]): Unit
  override def close(): Unit = {}
}

trait QueueItemProcessorBuilder[T] {
  def apply(index: Int): QueueItemProcessor[T]
}

object QueueItemProcessor {
  def stringToNode(item: Item[String]): Item[Option[Node]] = {
    Item(item.index, AstLoader.parseLine(item.content))
  }
}

