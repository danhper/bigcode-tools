package com.tuvistavie.astgenerator.visitors

import com.github.javaparser.JavaParser
import org.specs2.mutable.Specification
import collection.JavaConverters._

class IdentifierReplacementVisitorTest extends Specification {
  private val replacements = Map(
    "TYPE" -> IdentifierReplacementVisitor.typeToken,
    "ID" -> IdentifierReplacementVisitor.idToken,
    "ZERO" -> IdentifierReplacementVisitor.zeroToken,
    "NON_ZERO" -> IdentifierReplacementVisitor.nonZeroToken,
    "STRING_TOKEN" -> IdentifierReplacementVisitor.stringToken
  )

  "IdentifierReplacementVisitor" >> {
    "should replace class name in declaration" >> {
      transform("class Foo {}") must_== format("class <type> {}")
      transform("class Foo<Bar> {}") must_== format("class <type><<type>> {}")
      transform("class Foo extends Bar {}") must_== format("class <type> extends <type> {}")
      transform("class Foo implements Bar {}") must_== format("class <type> implements <type> {}")
    }

    "should replace method name in declaration" >> {
      val expected = format("class <type> { <type> <id>() { return new <type>();}}")
      transform("class Foo { Bar bar() { return new Bar(); } }") must_== expected
    }

    "should replace variable declaration types and identifiers" >> {
      transform("MyString a;") must_== format("<type> <id>;")
    }

    "should replace integer literals" >> {
      transform("int a = 123;") must_== format("int <id> = <non-zero>;")
      transform("int a = 0;") must_== format("int <id> = <zero>;")
    }

    "should replace double literals" >> {
      transform("double a = 12.3;") must_== format("double <id> = <non-zero>;")
      transform("double a = 0;") must_== format("double <id> = <zero>;")
      transform("double a = 0.0;") must_== format("double <id> = <zero>;")
    }

    "should replace String literals" >> {
      transform("String a = \"my random string\";") must_== format("<type> <id> = \"<string>\";")
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
