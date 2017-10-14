package com.tuvistavie.bigcode.asttools.ast

import java.nio.charset.CodingErrorAction
import java.nio.file.Path

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.tuvistavie.bigcode.asttools.models.{Node, Token}
import com.typesafe.scalalogging.LazyLogging

import scala.io.{Codec, Source}
import scala.util.Try
import scalaz.std.list._
import scalaz.std.option._
import scalaz.syntax.std.boolean._
import scalaz.syntax.traverse._


class AstLoader(array: Array[Map[String, Any]]) {
  private var nodeCache: Map[Int, Node] = Map.empty

  def generateAst(): Option[Node] = {
    generateAst(0, None)
  }

  private def generateAst(index: Int, parent: Option[Node]): Option[Node] = {
    def generateAst0(index: Int): Option[Node] = {
      for {
        jsonNode <- array.isDefinedAt(index).option(array(index))
        token <- generateToken(jsonNode)
        node = new Node(token, parent)
        children <- generateChildren(node, jsonNode.getOrElse("children", List.empty))
      } yield {
        node.children = children
        nodeCache += (index -> node)
        node
      }
    }

    nodeCache.get(index).orElse(generateAst0(index))
  }

  private def generateToken(jsonNode: Map[String, Any]): Option[Token] = (jsonNode.get("type"), jsonNode.get("value")) match {
    case (Some(tokenType: String), Some(tokenValue)) => Some(Token(tokenType, Some(tokenValue.toString)))
    case (Some(tokenType: String), None) => Some(Token(tokenType))
    case _ => None
  }

  private def generateChildren(parent: Node, rawChildren: Any): Option[List[Node]] = rawChildren match {
    case Nil => Some(List.empty)
    case (_: Int) :: _ => rawChildren.asInstanceOf[List[Int]].traverse(i => generateAst(i, Some(parent)))
    case _ => None
  }
}

object AstLoader extends LazyLogging {
  private implicit val codec: Codec = Codec("UTF-8")
  codec.onMalformedInput(CodingErrorAction.REPLACE)
  codec.onUnmappableCharacter(CodingErrorAction.REPLACE)

  val mapper: ObjectMapper = new ObjectMapper().registerModule(DefaultScalaModule)

  def loadFirst(filepath: Path): Option[Node] = loadOne(filepath.toString, 0)

  def loadOne(filepath: Path, index: Int): Option[Node] = loadOne(filepath.toString, index)
  def loadOne(filepath: String, index: Int): Option[Node] = {
    Source.fromFile(filepath).getLines().slice(index, index + 1).toStream.headOption.flatMap(parseLine)
  }

  def loadAll(filepath: Path): Iterator[Node] = loadAll(filepath.toString)
  def loadAll(filepath: String): Iterator[Node] = {
    Source.fromFile(filepath).getLines().flatMap(line => parseLine(line))
  }

  def parseLine(line: String): Option[Node] = {
    for {
      json <- Try(mapper.readValue(line, classOf[Array[Any]])).toOption
      normalizedJson = normalizeJson(json)
      ast <- new AstLoader(normalizedJson).generateAst()
    } yield {
      ast
    }
  }

  private def normalizeJson(lines: Array[Any]): Array[Map[String, Any]] = {
    removeTrailingZero(lines).map(line => line.asInstanceOf[Map[String, Any]])
  }

  // NOTE: for some reason, js150 array has the format [node1, node2,..., 0]
  private def removeTrailingZero(line: Array[Any]): Array[Any] = line match {
    case parsed if parsed.last == 0 => parsed.dropRight(1)
    case parsed => parsed
  }
}
