package com.tuvistavie.bigcode.asttools.models


import com.fasterxml.jackson.databind.ObjectMapper
import scalaz.syntax.std.boolean._

import scala.collection.immutable.StringOps

case class Vocabulary(
  items: Map[Int, VocabItem],
  strippedIdentifiers: Boolean
) {
  val size: Int = items.size

  lazy val vocabHash: Map[Token, Int] = items.map { case (index, item) => (item.token, index) }

  lazy val letters: Set[Token] = vocabHash.keySet

  def indexFor(token: Token): Int = {
    vocabHash.get(token).orElse(indexForType(token)).getOrElse(Vocabulary.unk)
  }

  private def indexForType(token: Token): Option[Int] = {
    token.value.flatMap(_ => vocabHash.get(token.copy(value = None)))
  }

  val totalLettersCount: Int = items.map(_._2.count).sum

  def toTSV: String = {
    val baseHeaders = List("id", "type", "metaType", "count")
    val headers = strippedIdentifiers.option(baseHeaders).getOrElse(baseHeaders :+ "value").mkString("\t")

    val rows = items.toList.sortBy(_._1).map { case (index, item) =>
      val token = item.token
      val baseRow = List(index.toString, token.tokenType, token.metaType, item.count.toString)
      val row = strippedIdentifiers.option(baseRow).getOrElse(
        baseRow :+ token.value.map(n => Vocabulary.mapper.writeValueAsString(n)).getOrElse(""))
      row.mkString("\t")
    }

    (headers :: rows).mkString("\n")
  }
}

object Vocabulary {
  val unk: Int = -1
  val mapper = new ObjectMapper()

  def apply(items: Seq[VocabItem], strippedIdentifiers: Boolean = false): Vocabulary = {
    val mapItems = items.zipWithIndex.map(_.swap).toMap
    Vocabulary(mapItems, strippedIdentifiers)
  }

  def fromTSV(tsv: String): Vocabulary = {
    val linesIterator = (tsv: StringOps).lines
    val headers = linesIterator.next().split("\t")
    val strippedIdentifiers = headers.length == 4
    val vocabItems = linesIterator.foldLeft(Map.empty[Int, VocabItem]) { case (items, row) =>
      val item = row.split("\t")
      val hasValue = item.isDefinedAt(4) && item(4).length > 0
      val token = Token(item(1), hasValue.option(mapper.readValue(item(4), classOf[String])))
      items + (item(0).toInt -> VocabItem(token, item(3).toInt))
    }
    new Vocabulary(vocabItems, strippedIdentifiers)
  }
}
