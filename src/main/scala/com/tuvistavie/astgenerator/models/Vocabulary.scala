package com.tuvistavie.astgenerator.models

case class Vocabulary(letters: Set[Subgraph]) {
  val size: Int = letters.size
}
