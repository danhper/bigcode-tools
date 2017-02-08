package com.tuvistavie.astgenerator

import java.io.FileInputStream
import java.nio.file.{Path, Paths}

import com.github.javaparser.JavaParser
import com.github.javaparser.ast.CompilationUnit
import com.tuvistavie.astgenerator.visitors.{DependencyVisitor, IdentifierReplacementVisitor}

class FileProcessor(private val compilationUnit: CompilationUnit) {
  import com.tuvistavie.astgenerator.util.JavaConversions._

  val packageName: String = compilationUnit.getPackageDeclaration.toOption.map(_.getName.toString).getOrElse("")

  override def toString: String = compilationUnit.toString

  def findDependencies(): Set[String] = {
    val dependencyVisitor = new DependencyVisitor(packageName)
    dependencyVisitor.visit(compilationUnit, null)
    dependencyVisitor.dependencies
  }

  def run() {
    val visitors = List(new IdentifierReplacementVisitor)
    visitors.foreach { visitor => visitor.visit(compilationUnit, null) }
  }
}

object FileProcessor {
  def apply(filepath: String): FileProcessor = FileProcessor(Paths.get(filepath))
  def apply(filepath: Path): FileProcessor = {
    val in = new FileInputStream(filepath.toFile)
    val compilationUnit = JavaParser.parse(in)
    new FileProcessor(compilationUnit)
  }
}
