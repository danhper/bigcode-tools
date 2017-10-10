package com.tuvistavie.bigcode.asttools.models

import com.tuvistavie.bigcode.asttools.BaseSpec

class TokenSpec extends BaseSpec {
  it("should be comparable") {
    Token("foo") shouldEqual Token("foo")
    Token("foo") shouldEqual Token("foo", None)
    Token("foo") shouldNot equal(Token("foo", Some("bar")))
  }

  describe("label") {
    it("should return value when present") {
      Token("foo", Some("bar")).label shouldEqual "bar"
    }

    it("should return type when no value") {
      Token("foo").label shouldEqual "foo"
    }
  }

  describe("metaType") {
    it("should return known metaTypes") {
      Token("FooExpr").metaType shouldEqual "Expr"
      Token("FooExpression").metaType shouldEqual "Expr"
      Token("FooStmt").metaType shouldEqual "Stmt"
      Token("FooStatement").metaType shouldEqual "Stmt"
      Token("FooDeclaration").metaType shouldEqual "Declaration"
    }

    it("should return other for unknown metaTypes") {
      Token("FooBar").metaType shouldEqual "Other"
    }
  }
}
