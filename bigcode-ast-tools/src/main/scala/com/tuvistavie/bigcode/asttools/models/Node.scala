package com.tuvistavie.bigcode.asttools.models

class Node(val token: Token, val parent: Option[Node] = None) {
  var children: List[Node] = List.empty

  override def toString: String = children match {
    case Nil => token.label
    case _   => f"${token.label} (${children.size} children)"
  }

  def getToken(stripIdentifiers: Boolean = false): Token = {
    if (stripIdentifiers) { Token(token.tokenType) } else { token }
  }
}


object Node {
  def apply(token: Token): Node = {
    new Node(token)
  }
}
