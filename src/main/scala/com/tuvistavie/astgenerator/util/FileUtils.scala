package com.tuvistavie.astgenerator.util

import java.io.FileInputStream
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes

import com.github.javaparser.ast.{CompilationUnit, Node}
import com.github.javaparser.{JavaParser, ParseProblemException}
import com.tuvistavie.astgenerator.ast.TokenExtractor
import com.tuvistavie.astgenerator.models.Subgraph
import com.typesafe.scalalogging.LazyLogging

import scala.collection.JavaConverters._

object FileUtils extends LazyLogging {
  def findFiles(root: Path): Set[Path] = findFiles(root,  tautology[Path] _)
  def findFiles(root: String): Set[Path] = findFiles(Paths.get(root))
  def findFiles(root: String, condition: Function[Path, Boolean]): Set[Path] = findFiles(Paths.get(root), condition)

  def findFiles(root: Path, condition: Function[Path, Boolean]): Set[Path] = {
    val paths = Set.newBuilder[Path]
    Files.walkFileTree(root, new SimpleFileVisitor[Path] {
      override def visitFile(path: Path, basicFileAttributes: BasicFileAttributes): FileVisitResult = {
        if (condition(path)) {
          paths += path
        }
        super.visitFile(path, basicFileAttributes)
      }
    })
    paths.result()
  }

  def parseFile(filepath: Path): Option[CompilationUnit] = {
    try {
      Some(JavaParser.parse(new FileInputStream(filepath.toFile)))
    } catch {
      case _: ParseProblemException =>
        logger.error(s"failed to parse $filepath")
        None
      case _: StackOverflowError =>
        logger.error(s"stack overflow on $filepath")
        None
    }
  }

  def parseFileToSubgraph(filepath: String): Option[Subgraph] = parseFileToSubgraph(Paths.get(filepath))
  def parseFileToSubgraph(filepath: Path): Option[Subgraph] = {
    parseFile(filepath).map(nodeToSubgraph)
  }

  def nodeToSubgraph(node: Node): Subgraph = {
    val children = node.getChildNodes.asScala.map(nodeToSubgraph).toList
    Subgraph(TokenExtractor.extractToken(node), children)
  }

  def withExtension(extension: String): Function[Path, Boolean] = {
    (path: Path) => path.toString.endsWith(s".$extension")
  }

  private def tautology[T](arg: T): Boolean = true
}
