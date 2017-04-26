package com.tuvistavie.astgenerator.visitors

import com.github.javaparser.JavaParser
import collection.JavaConverters._

import org.scalatest.{FunSpec, Matchers}

class IdentifierReplacementVisitorSpec extends FunSpec with Matchers {
  private val replacements = Map(
    "TYPE" -> IdentifierReplacementVisitor.typeToken,
    "ID" -> IdentifierReplacementVisitor.idToken,
    "ZERO" -> IdentifierReplacementVisitor.zeroToken,
    "NON_ZERO" -> IdentifierReplacementVisitor.nonZeroToken,
    "STRING_TOKEN" -> IdentifierReplacementVisitor.stringToken
  )

  describe("IdentifierReplacementVisitor") {
    it("should replace class name in declaration") {
      transform("class Foo {}") should equal(format("class <type> {}"))
      transform("class Foo<Bar> {}") should equal(format("class <type><<type>> {}"))
      transform("class Foo extends Bar {}") should equal(format("class <type> extends <type> {}"))
      transform("class Foo implements Bar {}") should equal(format("class <type> implements <type> {}"))
    }

    it("should replace method name in declaration") {
      val expected = format("class <type> { <type> <id>() { return new <type>();}}")
      transform("class Foo { Bar bar() { return new Bar(); } }") should equal(expected)
    }

    it("should replace variable declaration types and identifiers") {
      transform("MyString a;") should equal(format("<type> <id>;"))
    }

    it("should replace integer literals") {
      transform("int a = 123;") should equal(format("int <id> = <non-zero>;"))
      transform("int a = 0;") should equal(format("int <id> = <zero>;"))
    }

    it("should replace double literals")  {
      transform("double a = 12.3;") should equal(format("double <id> = <non-zero>;"))
      transform("double a = 0;") should equal(format("double <id> = <zero>;"))
      transform("double a = 0.0;") should equal(format("double <id> = <zero>;"))
    }

    it("should replace String literals") {
      transform("String a = \"my random string\";") should equal(format("<type> <id> = \"<string>\";"))
    }
  }

  private def transform(input: String): String = {
    val block = JavaParser.parseBlock(s"{ $input }")
    block.accept(new IdentifierReplacementVisitor, null)
    block.getChildNodes.asScala.map(_.toString).mkString("\n")
  }

  private def format(input: String): String = {
    val parsableInput = replacements.foldLeft(input) { case (acc, (k, v)) => acc.replace(v, k) }
    val block = JavaParser.parseBlock(s"{ $parsableInput }")
    val output = block.getChildNodes.asScala.map(_.toString).mkString("\n")
    replacements.foldLeft(output) { case (acc, (k, v)) => acc.replace(k, v) }
  }
}
