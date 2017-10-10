package com.tuvistavie.bigcode.asttools.ast

import com.tuvistavie.bigcode.asttools.BaseSpec

class AstLoaderSpec extends BaseSpec {
  describe("parseLine") {
    it("should return None on invalid JSON") {
      AstLoader.parseLine("invalid json") shouldEqual None
    }

    it("should return None on invalid input") {
      AstLoader.parseLine("""[{"type": "foo", "children": [1]}]""") shouldEqual None
    }

    it("should handle tokenType and value") {
      val optNode = AstLoader.parseLine("""[{"type": "foo", "value": "bar"}]""")
      optNode shouldNot equal(None)
      val node = optNode.get
      node.token.tokenType shouldEqual "foo"
      node.token.value shouldEqual Some("bar")
      node.children.size shouldEqual 0
    }

    it("should handle trailing 0") {
      val optNode = AstLoader.parseLine("""[{"type": "foo"}, 0]""")
      optNode shouldNot equal(None)
      val node = optNode.get
      node.token.tokenType shouldEqual "foo"
      node.token.value shouldEqual None
      node.children.size shouldEqual 0
    }

    it("should handle children and set parent correctly") {
      val optNode = AstLoader.parseLine("""[{"type": "foo", "children": [1]}, {"type": "bar", "value": "baz"}]""")
      optNode shouldNot equal(None)
      val node = optNode.get

      node.token.tokenType shouldEqual "foo"
      node.token.value shouldEqual None
      node.children.size shouldEqual 1

      val child = node.children.head
      child.parent shouldEqual Some(node)
      child.token.tokenType shouldEqual "bar"
      child.token.value shouldEqual Some("baz")
      child.children.size shouldEqual 0
    }
  }
}
