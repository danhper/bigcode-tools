package com.tuvistavie.bigcode.asttools

import java.nio.file.{Path, Paths}

import org.scalatest.{FunSpec, Matchers}

class BaseSpec extends FunSpec with Matchers {
  val fixturesRoot: Path = Paths.get(getClass.getResource("/fixtures").getPath)
  val circleFixturePath: Path = Paths.get(fixturesRoot.toString, "Circle.js.json")
}
