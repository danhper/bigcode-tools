package com.tuvistavie.bigcode.asttools.ast

import java.awt.Desktop
import java.io.{ByteArrayOutputStream, File, IOException}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}

import com.tuvistavie.bigcode.asttools.models.{Node, Token, VisualizeAstConfig}
import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.io.FilenameUtils
import org.apache.commons.text.StringEscapeUtils

import scala.io.{Source, StdIn}
import scala.sys.process._

object AstVisualizer {
  def visualizeAst(config: VisualizeAstConfig): Unit = {
    new AstVisualizer(config).visualizeAst()
  }
}

class AstVisualizer(config: VisualizeAstConfig) extends LazyLogging {
  private val imageExtensions = Set("png", "jpg", "svg")

  private lazy val astIndex: Either[String, Int] = {
    (config.index, config.filename) match {
      case (None, None) => Right(0)
      case (Some(n), _) => Right(n)
      case (_, Some(filename)) => findIndex(filename)
    }
  }

  private lazy val parsedAst: Either[String, Node] = {
    config.input match {
      case "<STDIN>" => AstLoader.parseLine(StdIn.readLine()).toRight(s"could not parse AST from stdin")
      case _ =>
        astIndex.flatMap(i => AstLoader.loadOne(config.input, i).toRight(s"could not parse AST at index $i in ${config.input}"))
    }
  }

  def visualizeAst(): Unit = {
    parsedAst match {
      case Right(root) =>
        val dot = generateDot(root)

        if (config.debug) {
          println(dot)
        }

        config.fileOutput.foreach { output =>
          outputDot(dot, output)
          if (config.view) { openOutput(output) }
        }
      case Left(err) =>
        logger.error(err)
    }
  }

  def generateDot(root: Node): String = {
    val links = generateLinks(root).map { case (n1, n2) =>
      s"${nodeId(n1)} -> ${nodeId(n2)};"
    }
    val nodes = generateNodes(root).map(formatNode(_, config.hideIdentifiers))

    val filename = Paths.get(config.input).getFileName.toString.replaceAll("[. -]", "_")
    val dot = s"""digraph $filename {
        ${nodes.mkString("\n")}
        ${links.mkString("\n")}
    }
    """.stripMargin

    dot
  }

  private def findIndex(filename: String): Either[String, Int] = {
    val filesListPath = config.filesListPath.getOrElse(FilenameUtils.removeExtension(config.input) + ".txt")
    val result = Source.fromFile(filesListPath).getLines.zipWithIndex.find(_._1 == filename).map(_._2)
    result.toRight(s"$filename was not found in $filesListPath")
  }

  private def outputDot(dot: String, output: String): Unit = {
    output match {
      case name if name.endsWith(".dot") =>
        writeDot(dot, name)
      case name if imageExtensions.contains(FilenameUtils.getExtension(name)) =>
        writeImage(dot, name)
      case _ =>
        System.err.println(s"unsupported format for $output")
    }
  }

  private def openOutput(filePath: String): Unit = {
    try {
      Desktop.getDesktop.open(new File(filePath))
    } catch {
      case _: IOException => println(s"could not open $filePath")
    }
  }

  private def writeImage(dot: String, output: String): Unit = {
    if (!dotAvailable) {
      throw new IOException("dot (from graphviz) must be available on the path to output images")
    }

    var file: Option[Path] = None
    try {
      val extension = FilenameUtils.getExtension(output)
      file = Some(Files.createTempFile(s"dot.$extension", System.nanoTime().toString))
      file.foreach(runDot(dot, _, extension, output) match {
        case (_, 0) =>
        case (commandOutput, exitValue) =>
          throw new IOException(s"failed to run dot with exit code $exitValue: $commandOutput")
      })
    } finally {
      file.foreach(f => f.toFile.delete())
    }
  }

  private def runDot(dot: String, tempFile: Path, extension: String, output: String): (String, Int) = {
    writeDot(dot, tempFile)
    val processBuilder = Process(Seq("dot", s"-T$extension", tempFile.toString, "-o", output))
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
    StringEscapeUtils.escapeJson(formatToken(node.token, hideIdentifiers))
  }

  private def formatToken(token: Token, hideIdentifiers: Boolean) = {
    if (hideIdentifiers) { token.tokenType } else { token.label }
  }

  private def nodeId(node: Node): Int = System.identityHashCode(node)

  private def generateNodes(node: Node): List[Node] = {
    node +: node.children.flatMap(generateNodes)
  }

  private def generateLinks(node: Node): List[(Node, Node)] = {
    node.children.flatMap { n: Node => (node, n) +: generateLinks(n) }
  }
}
