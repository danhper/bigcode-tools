package com.tuvistavie.astgenerator.visitors

import com.github.javaparser.ast.ImportDeclaration
import com.github.javaparser.ast.body.VariableDeclarator
import com.github.javaparser.ast.visitor.VoidVisitorAdapter

class DependencyVisitor(packageName: String) extends VoidVisitorAdapter[Void] {
  private val dependencies_ = Set.newBuilder[String]

  override def visit(n: ImportDeclaration, arg: Void) {
    println(n.getName)
  }

  override def visit(n: VariableDeclarator, arg: Void): Unit = {
    println(n.getType)
  }

  def dependencies = dependencies_.result()
}
