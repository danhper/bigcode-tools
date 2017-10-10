package com.tuvistavie.astgenerator.models

case class Subgraph(token: Token, var children: List[Subgraph] = List.empty, parent: Option[Subgraph] = None) {
  override def toString: String = children match {
    case Nil => token.label
    case _   => f"${token.label} (${children.size} children)"
  }
}
