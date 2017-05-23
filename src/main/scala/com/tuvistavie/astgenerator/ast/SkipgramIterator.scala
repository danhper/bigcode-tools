package com.tuvistavie.astgenerator.ast

import java.nio.file.Path

import com.tuvistavie.astgenerator.SkipgramConfig
import com.tuvistavie.astgenerator.models.Vocabulary
import com.tuvistavie.astgenerator.util.FileUtils

class SkipgramIterator(vocabulary: Vocabulary, skipgramConfig: SkipgramConfig) {
  val files: Set[Path] = FileUtils.findFiles(skipgramConfig.project, FileUtils.withExtension("java"))
}

object SkipgramIterator {
  def apply(vocabulary: Vocabulary, config: SkipgramConfig): SkipgramIterator = {
    new SkipgramIterator(vocabulary, config)
  }
}
