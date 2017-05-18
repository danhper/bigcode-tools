package com.tuvistavie.astgenerator.models

import com.github.javaparser.ast.Node

case class Token(tokenType: String, value: Option[String] = None) {
  def label: String = value match {
    case Some(v) => v
    case None => tokenType
  }
}

object Token {
  def apply(node: Node): Token = {
    Token(node.getClass.getSimpleName, None)
  }
  def apply(node: Node, value: String): Token = {
    Token(node.getClass.getSimpleName, Some(value))
  }
  def apply(node: Node, value: Option[String]): Token = {
    Token(node.getClass.getSimpleName, value)
  }
}
