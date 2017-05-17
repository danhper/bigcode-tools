package com.tuvistavie.astgenerator

import java.io.FileInputStream
import java.nio.file.{Path, Paths}

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.{CompilationUnit, Node}
import com.github.javaparser.{JavaParser, JavaToken}
import com.tuvistavie.astgenerator.visitors.ExtractTokenVisitor


class TokenExtractor(val filepath: Path) {
  val parsed: CompilationUnit = JavaParser.parse(new FileInputStream(filepath.toFile))


  def extractTokens: Set[JavaToken] = {
    // val result = JavaParser.parse(input)
    // result.getTokens.toOption.map(list => list.asScala).getOrElse(Seq.empty).toSet
    Set.empty
  }

  def simpleTree(): Unit = {
    val visitor = new ExtractTokenVisitor()
    parsed.getChildNodes.forEach {
      case n: ClassOrInterfaceDeclaration => simpleTree(n, visitor)
      case _ =>
    }
  }

  def simpleTree(node: Node, visitor: ExtractTokenVisitor): Unit = {
    println(node.getClass.toString + " " + node.accept(visitor, null))
    node.getChildNodes.forEach(node => simpleTree(node, visitor))
  }


//  def generateVocabulary(): Set[String] = {
//    val cu = JavaParser.parse(input)
//  }

  // private def generateVocabulary(node: Node, )
}

object TokenExtractor {
  def apply(filepath: String): TokenExtractor = TokenExtractor(Paths.get(filepath))
  def apply(filepath: Path): TokenExtractor = new TokenExtractor(filepath)
}
