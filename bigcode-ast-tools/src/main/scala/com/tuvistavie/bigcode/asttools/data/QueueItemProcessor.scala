package com.tuvistavie.bigcode.asttools.data

import com.tuvistavie.bigcode.asttools.models.Node
import com.tuvistavie.bigcode.asttools.ast.AstLoader

object QueueItemProcessor {
  def stringToNode(item: Item[String]): Item[Option[Node]] = {
    Item(item.index, AstLoader.parseLine(item.content))
  }
}

