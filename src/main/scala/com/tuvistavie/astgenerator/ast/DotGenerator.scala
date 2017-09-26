package com.tuvistavie.astgenerator.ast

import java.awt.Desktop
import java.io.{ByteArrayOutputStream, File, IOException}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}

import com.tuvistavie.astgenerator.models.{GenerateDotConfig, Subgraph, Token}
import com.tuvistavie.astgenerator.util.FileUtils
import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.io.FilenameUtils

import scala.sys.process._

object DotGenerator {
  def generateDot(config: GenerateDotConfig): Unit = {
    new DotGenerator(config).generateDot()
  }
}

class DotGenerator(config: GenerateDotConfig) extends LazyLogging {
  private lazy val parsedSubgraph: Option[Subgraph] = {
    if (FilenameUtils.getExtension(config.filepath) == "json") {
      ASTLoader.loadOne(config.filepath, config.index)
    } else {
      FileUtils.parseFileToSubgraph(config.filepath)
    }
  }

  def generateDot(): Unit = {
    parsedSubgraph match {
      case Some(graph) =>
        val dot = generateDot(graph)
        if (config.debug) {
          println(dot)
        }
      case None => logger.error(s"could not parse ${config.filepath}")
    }
  }

  def generateDot(parsed: Subgraph): String = {
    val links = generateLinks(parsed).map { case (n1, n2) =>
      s"${nodeId(n1)} -> ${nodeId(n2)};"
    }
    val nodes = generateNodes(parsed).map(formatNode(_, config.hideIdentifiers))

    val filename = Paths.get(config.filepath).getFileName.toString
    val dot = s"""digraph ${filename.replace(".", "_")} {
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

  private def formatNode(node: Subgraph, hideIdentifiers: Boolean): String = {
    s"""${nodeId(node)} [label = "${formatLabel(node, hideIdentifiers)}"];"""
  }

  private def formatLabel(node: Subgraph, hideIdentifiers: Boolean) = {
    formatToken(node.token, hideIdentifiers)
  }

  private def formatToken(token: Token, hideIdentifiers: Boolean) = {
    if (hideIdentifiers) { token.tokenType } else { token.label }
  }

  private def nodeId(node: Subgraph): Int = System.identityHashCode(node)

  private def generateNodes(node: Subgraph): List[Subgraph] = {
    node +: node.children.flatMap(generateNodes)
  }

  private def generateLinks(node: Subgraph): List[(Subgraph, Subgraph)] = {
    node.children.flatMap { n: Subgraph => (node, n) +: generateLinks(n) }
  }
}
