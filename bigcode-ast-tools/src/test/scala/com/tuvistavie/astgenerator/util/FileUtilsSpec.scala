package com.tuvistavie.astgenerator.util

import java.nio.file.{Path, Paths}

import org.scalatest.{FunSpec, Matchers}

class FileUtilsSpec extends FunSpec with Matchers {
  val root: Path = Paths.get(getClass.getResource("/fixtures/dummy_project").getPath)

  describe("findFiles") {
    it("should list all files in the directory") {
      val expected = Set("Main.java", "Main.class", "MyClass.java", "MainTest.java", "MyClassTest.java")
      FileUtils.findFiles(root).map { f => f.getFileName.toString } should equal (expected)
    }

    it("should filter files when predicate given") {
      val expected = Set("Main.java", "MyClass.java", "MainTest.java", "MyClassTest.java")
      FileUtils.findFiles(root, (p: Path) => p.toString.endsWith(".java")).map { f => f.getFileName.toString } should equal (expected)
      FileUtils.findFiles(root, FileUtils.withExtension("java")).map { f => f.getFileName.toString } should equal (expected)
    }
  }
}
