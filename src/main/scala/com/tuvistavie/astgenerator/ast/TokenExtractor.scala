package com.tuvistavie.astgenerator.ast

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.expr._
import com.github.javaparser.ast.nodeTypes.NodeWithIdentifier
import com.tuvistavie.astgenerator.models.Token


object TokenExtractor {
  def extractToken(node: Node): Token = node match {
    case n: BinaryExpr => Token(n, n.getOperator.asString)
    case n: BooleanLiteralExpr => Token(n, n.getValue.toString)
    case n: NodeWithIdentifier[_] => Token(n, n.getIdentifier)
    case n: IntegerLiteralExpr => Token(n, safeNumToString(n.getValue.toInt))
    case n: DoubleLiteralExpr => Token(n, safeNumToString(n.getValue.toDouble))
    case n => Token(n)
  }

  private def safeNumToString[A](n: => A)(implicit num: Numeric[A]): Option[String] = {
    util.Try(n).map(numToString(_)(num)).toOption
  }

  private def numToString[A](n: A)(implicit num: Numeric[A]): String = n match {
    case 0 => "0"
    case _ => "NON-ZERO"
  }
}
