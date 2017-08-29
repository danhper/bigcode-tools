package com.tuvistavie.astgenerator.util

import java.io.FileInputStream
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file._

import com.github.javaparser.{JavaParser, ParseProblemException}
import com.github.javaparser.ast.CompilationUnit
import com.typesafe.scalalogging.LazyLogging

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

  def withExtension(extension: String): Function[Path, Boolean] = {
    (path: Path) => path.toString.endsWith(s".$extension")
  }

  private def tautology[T](arg: T): Boolean = true
}
