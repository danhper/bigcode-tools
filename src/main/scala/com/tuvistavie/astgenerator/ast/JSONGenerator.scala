package com.tuvistavie.astgenerator.ast

import java.io.{File, FileInputStream}
import java.nio.file.{Path, Paths}

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper, ObjectWriter}
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.CompilationUnit
import com.tuvistavie.astgenerator.util.FileUtils
import com.tuvistavie.astgenerator.{Config, GenerateAstConfig}
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

  def run(config: GenerateAstConfig): Unit = {
    val files = FileUtils.findFiles(config.project, FileUtils.withExtension("java"))
    val projectPath = Paths.get(config.project)
    val result = JsonNodeFactory.instance.objectNode()
    files map { f => (f, processFile(f, config)) } foreach {
      case (f, r) => result.set(projectPath.relativize(f).toString, r)
    }
    val writer = makeWriter(config)
    val outputFile = new File(config.output)
    val output = makeOutput(config, result)
    writer.writeValue(outputFile, output)
  }

  private def processFile(path: Path, config: GenerateAstConfig): JsonNode = {
    val processor = JSONGenerator(path, config)
    processor.run()
    processor.toJson
  }

  private def makeWriter(config: GenerateAstConfig): ObjectWriter = {
    val mapper = new ObjectMapper()
    if (config.pretty) mapper.writerWithDefaultPrettyPrinter() else mapper.writer()
  }

  private def makeOutput(config: GenerateAstConfig, result: JsonNode): JsonNode = {
    val output = JsonNodeFactory.instance.objectNode()
    output.put("project", config.project)
    output.put("version", Config.formatVersion)
    output.set("result", result)
    output
  }
}
