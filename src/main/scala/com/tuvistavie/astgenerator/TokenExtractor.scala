package com.tuvistavie.astgenerator

import java.io.{FileInputStream, InputStream}
import java.nio.file.{Path, Paths}

import com.github.javaparser.{JavaParser, JavaToken, ParseStart, Providers}
import com.tuvistavie.astgenerator.util.JavaConversions._

import scala.collection.JavaConverters._


class TokenExtractor(val input: InputStream) {
  def extractTokens: Seq[JavaToken] = {
    val result = new JavaParser().parse(ParseStart.COMPILATION_UNIT, Providers.provider(input))
    result.getTokens.toOption.map(list => list.asScala).getOrElse(Seq.empty)
  }
}

object TokenExtractor {
  def apply(filepath: String): TokenExtractor = TokenExtractor(Paths.get(filepath))
  def apply(filepath: Path): TokenExtractor = {
    val in = new FileInputStream(filepath.toFile)
    new TokenExtractor(in)
  }
}
