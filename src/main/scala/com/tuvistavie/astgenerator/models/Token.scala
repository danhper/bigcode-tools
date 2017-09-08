package com.tuvistavie.astgenerator.models

import com.github.javaparser.ast.Node

case class Token(tokenType: String, value: Option[String] = None) {
  def label: String = value match {
    case Some(v) => v
    case None => tokenType
  }

  def metaType: String = tokenType match {
    case t if t.endsWith("Stmt") => "Stmt"
    case t if t.endsWith("Expr") => "Expr"
    case t if t.endsWith("Type") => "Type"
    case t if t.endsWith("Declaration") => "Declaration"
    case _ => "Other"
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
