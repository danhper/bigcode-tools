package com.tuvistavie.astgenerator

import com.fasterxml.jackson.databind.ObjectMapper
import com.tuvistavie.astgenerator.util.FileUtils

object Main {
  def main(args: Array[String]): Unit = {
    val entryPoints = List("/home/daniel/Dropbox/Programming/Java/Othello/src")

    entryPoints foreach { entryPoint =>
      val paths = FileUtils.findFiles(entryPoint, FileUtils.withExtension("java"))
      val processor = FileProcessor(paths.toList(1))
      processor.run()
      val mapper = new ObjectMapper()
      println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(processor.toJson()))
    }
  }
}
