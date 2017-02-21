package com.tuvistavie.astgenerator

import java.io.FileInputStream
import java.nio.file.{Path, Paths}

import com.fasterxml.jackson.databind.JsonNode
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.CompilationUnit
import com.tuvistavie.astgenerator.visitors.{DependencyVisitor, IdentifierReplacementVisitor, JsonVisitor}

class FileProcessor(val compilationUnit: CompilationUnit) {
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
    visitors.foreach { visitor => compilationUnit.accept(visitor, null) }
  }

  def toJson: JsonNode = {
    compilationUnit.accept(new JsonVisitor, null)
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
