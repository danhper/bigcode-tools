package com.tuvistavie.astgenerator.ast

import java.io.FileInputStream
import java.nio.file.{Path, Paths}

import com.github.javaparser.ast.expr._
import com.github.javaparser.ast.nodeTypes.NodeWithIdentifier
import com.github.javaparser.ast.{CompilationUnit, Node}
import com.github.javaparser.{JavaParser, JavaToken}
import com.tuvistavie.astgenerator.models.Token


class TokenExtractor(val filepath: Path) {
  val parsed: CompilationUnit = JavaParser.parse(new FileInputStream(filepath.toFile))


  def extractTokens: Set[JavaToken] = {
    // val result = JavaParser.parse(input)
    // result.getTokens.toOption.map(list => list.asScala).getOrElse(Seq.empty).toSet
    Set.empty
  }



//  def generateVocabulary(): Set[String] = {
//    val cu = JavaParser.parse(input)
//  }

  // private def generateVocabulary(node: Node, )
}

object TokenExtractor {
  def apply(filepath: String): TokenExtractor = TokenExtractor(Paths.get(filepath))
  def apply(filepath: Path): TokenExtractor = new TokenExtractor(filepath)

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
