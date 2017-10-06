package com.tuvistavie.astgenerator.ast

import java.nio.file.Path

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.tuvistavie.astgenerator.models.{Subgraph, Token}
import com.typesafe.scalalogging.LazyLogging

import scala.io.Source
import scalaz.syntax.std.boolean._


class ASTLoader(array: Array[Map[String, Any]]) {
  private var nodeCache: Map[Int, Subgraph] = Map.empty


  def generateSubgraph(index: Int = 0): Option[Subgraph] = {
    def generateSubgraph0(index: Int): Option[Subgraph] = {
      for {
        node <- array.isDefinedAt(index).option(array(index))
        token <- generateToken(node)
        children <- generateChildren(node.getOrElse("children", List.empty))
      } yield {
        val subgraph = Subgraph(token, children)
        nodeCache += (index -> subgraph)
        subgraph
      }
    }

    nodeCache.get(index).orElse(generateSubgraph0(index))
  }


  private def generateToken(node: Map[String, Any]): Option[Token] = (node.get("type"), node.get("value")) match {
    case (Some(tokenType: String), Some(tokenValue: String)) => Some(Token(tokenType, Some(tokenValue)))
    case (Some(tokenType: String), None) => Some(Token(tokenType))
    case _ => None
  }

  private def generateChildren(rawChildren: Any): Option[List[Subgraph]] = rawChildren match {
    case (head: Int) :: tail =>
      generateChildren(tail).flatMap(rest => generateSubgraph(head).map(subgraph => subgraph +: rest))
    case Nil => Some(List.empty)
    case _ => None
  }
}

object ASTLoader extends LazyLogging {
  val mapper: ObjectMapper = new ObjectMapper().registerModule(DefaultScalaModule)

  def loadOne(filepath: Path, index: Int): Option[Subgraph] = loadOne(filepath.toString, index)
  def loadOne(filepath: String, index: Int): Option[Subgraph] = {
    Source.fromFile(filepath).getLines().slice(index, index + 1).toStream.headOption.flatMap(parseLine)
  }

  def loadAll(filepath: Path): Iterator[Subgraph] = loadAll(filepath.toString)
  def loadAll(filepath: String): Iterator[Subgraph] = {
    Source.fromFile(filepath).getLines().flatMap(line => parseLine(line))
  }

  def parseLine(line: String): Option[Subgraph] = parseLine(0, line)
  def parseLine(index: Int, line: String): Option[Subgraph] = {
    // NOTE: for some reason, js150 array has the format [node1, node2,..., 0]
    val result = (mapper.readValue(line, classOf[Array[Any]]) match {
      case parsed if parsed.last == 0 => parsed.dropRight(1)
      case parsed => parsed
    }).map(v => v.asInstanceOf[Map[String, Any]])

    try {
      new ASTLoader(result).generateSubgraph()
    } catch {
      case e: Throwable =>
        logger.error(s"failed to parse entry $index: $e")
        None
    }
  }
}
