package com.tuvistavie.astgenerator.ast

import java.awt.Desktop
import java.io.{ByteArrayOutputStream, File, FileInputStream, IOException}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}

import com.github.javaparser.JavaParser
import com.github.javaparser.ast.{CompilationUnit, Node}
import com.tuvistavie.astgenerator.models.GenerateDotConfig

import scala.collection.JavaConverters._
import scala.sys.process._


class DotGenerator(val filepath: Path) {
  val parsed: CompilationUnit = JavaParser.parse(new FileInputStream(filepath.toFile))

  def generateDot(config: GenerateDotConfig): String = {
    val links = generateLinks(parsed).map { case (n1, n2) =>
      s"${nodeId(n1)} -> ${nodeId(n2)};"
    }
    val nodes = generateNodes(parsed).map(formatNode(_, config.hideIdentifiers))

    val dot = s"""digraph ${filepath.getFileName.toString.replace(".", "_")} {
        ${nodes.mkString("\n")}
        ${links.mkString("\n")}
    }
    """.stripMargin

    config.fileOutput.foreach { output =>
      outputDot(dot, output)
      if (config.view) { openOutput(output) }
    }

    dot
  }

  private def outputDot(dot: String, output: String): Unit = {
    output match {
      case name if name.endsWith(".dot") =>
        writeDot(dot, name)
      case name if name.endsWith(".png") =>
        writePng(dot, name)
      case _ =>
        System.err.println(s"unsupported format for $output")
    }
  }

  private def openOutput(filePath: String): Unit = {
    Desktop.getDesktop.open(new File(filePath))
  }

  private def writePng(dot: String, output: String): Unit = {
    if (!dotAvailable) {
      throw new IOException("dot (from graphviz) must be available on the path to output png")
    }

    var file: Option[Path] = None
    try {
      file = Some(Files.createTempFile("dot.png", System.nanoTime().toString))
      file.foreach(runDot(dot, _, output) match {
        case (_, 0) =>
        case (commandOutput, exitValue) =>
          throw new IOException(s"failed to run dot with exit code $exitValue: $commandOutput")
      })
    } finally {
      file.foreach(f => f.toFile.delete())
    }
  }

  private def runDot(dot: String, tempFile: Path, output: String): (String, Int) = {
    writeDot(dot, tempFile)
    val processBuilder = Process(Seq("dot", "-Tpng", tempFile.toString, "-o", output))
    val result = processBuilder.run()
    (processBuilder.lineStream.mkString("\n"), result.exitValue())
  }

  private def dotAvailable: Boolean = {
    val cmd = "which dot" #> new ByteArrayOutputStream()
    cmd.! == 0
  }

  private def writeDot(dot: String, output: String): Unit = writeDot(dot, Paths.get(output))
  private def writeDot(dot: String, output: Path): Unit = {
    Files.write(output, dot.getBytes(StandardCharsets.UTF_8))
  }

  private def formatNode(node: Node, hideIdentifiers: Boolean): String = {
    s"""${nodeId(node)} [label = "${formatLabel(node, hideIdentifiers)}"];"""
  }

  private def formatLabel(node: Node, hideIdentifiers: Boolean) = {
    val token = TokenExtractor.extractToken(node)
    if (hideIdentifiers) { token.tokenType } else { token.label }
  }

  private def nodeId(node: Node): Int = System.identityHashCode(node)

  private def generateNodes(node: Node): List[Node] = {
    { node +: node.getChildNodes.asScala.flatMap(generateNodes) }.toList
  }

  private def generateLinks(node: Node): List[(Node, Node)] = {
    node.getChildNodes.asScala.flatMap { n: Node => (node, n) +: generateLinks(n) }.toList
  }
}

object DotGenerator {
  def apply(filepath: String): DotGenerator = DotGenerator(Paths.get(filepath))
  def apply(filepath: Path): DotGenerator = new DotGenerator(filepath)

  def run(config: GenerateDotConfig): Unit = {
    val dotGenerator = DotGenerator(config.filepath)
    val dot = dotGenerator.generateDot(config)
    if (config.debug) {
      println(dot)
    }
  }
}
