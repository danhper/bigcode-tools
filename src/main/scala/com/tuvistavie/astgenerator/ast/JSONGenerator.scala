package com.tuvistavie.astgenerator.ast

import java.io.FileInputStream
import java.nio.file.{Path, Paths}

import com.fasterxml.jackson.databind.JsonNode
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.CompilationUnit
import com.tuvistavie.astgenerator.GenerateAstConfig
import com.tuvistavie.astgenerator.visitors.{DependencyVisitor, IdentifierReplacementVisitor, JsonVisitor}

class JSONGenerator(val compilationUnit: CompilationUnit, val config: GenerateAstConfig) {
  import com.tuvistavie.astgenerator.util.JavaConversions._

  val packageName: String = compilationUnit.getPackageDeclaration.toOption.map(_.getName.toString).getOrElse("")

  override def toString: String = compilationUnit.toString

  def findDependencies(): Set[String] = {
    val dependencyVisitor = new DependencyVisitor(packageName)
    dependencyVisitor.visit(compilationUnit, null)
    dependencyVisitor.dependencies
  }

  def run() {
    if (!config.keepIdentifiers) {
      compilationUnit.accept(new IdentifierReplacementVisitor, null)
    }
  }

  def toJson: JsonNode = {
    compilationUnit.accept(new JsonVisitor, null)
  }
}

object JSONGenerator {
  def apply(filepath: String, config: GenerateAstConfig): JSONGenerator = JSONGenerator(Paths.get(filepath), config)
  def apply(filepath: Path, config: GenerateAstConfig): JSONGenerator = {
    val in = new FileInputStream(filepath.toFile)
    val compilationUnit = JavaParser.parse(in)
    new JSONGenerator(compilationUnit, config)
  }
}
