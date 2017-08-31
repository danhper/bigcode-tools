package com.tuvistavie.astgenerator.models

case class Subgraph(token: Token, children: List[Subgraph] = List.empty) {
  override def toString: String = children match {
    case Nil => token.label
    case _   => f"${token.label} (${children.size} children)"
  }
}
