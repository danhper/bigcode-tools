package com.tuvistavie.astgenerator.util

import java.nio.file.{Path, Paths}

class FileUtils$Test extends org.specs2.mutable.Specification {
  val root: Path = Paths.get(getClass.getResource("/fixtures/dummy_project").getPath)

  "findFiles" >> {
    "should list all files in the directory" >> {
      val expected = Set("Main.java", "Main.class", "MyClass.java", "MainTest.java", "MyClassTest.java")
      FileUtils.findFiles(root).map { f => f.getFileName.toString } must_== expected
    }

    "should filter files when predicate given" >> {
      val expected = Set("Main.java", "MyClass.java", "MainTest.java", "MyClassTest.java")
      FileUtils.findFiles(root, (p: Path) => p.toString.endsWith(".java")).map { f => f.getFileName.toString } must_== expected
      FileUtils.findFiles(root, FileUtils.withExtension("java")).map { f => f.getFileName.toString } must_== expected
    }
  }
}
