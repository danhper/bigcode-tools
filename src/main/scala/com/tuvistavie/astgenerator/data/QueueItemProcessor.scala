package com.tuvistavie.astgenerator.data

import com.tuvistavie.astgenerator.ast.ASTLoader
import com.tuvistavie.astgenerator.models.Subgraph

object QueueItemProcessor {
  def stringToSubgraph(item: Item[String]): Item[Option[Subgraph]] = {
    Item(item.index, ASTLoader.parseLine(item.content))
  }
}

