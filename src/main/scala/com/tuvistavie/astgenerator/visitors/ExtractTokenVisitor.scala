package com.tuvistavie.astgenerator.visitors

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.expr._
import com.github.javaparser.ast.visitor.GenericVisitorAdapter
import com.tuvistavie.astgenerator.models.NodeInfo

class ExtractTokenVisitor extends GenericVisitorAdapter[NodeInfo, Void] {
  override def visit(binaryExpr: BinaryExpr, arg: Void): NodeInfo = {
    NodeInfo(token = Some(binaryExpr.getOperator.asString()), isBlockRoot = true)
  }

  override def visit(booleanLiteralExpr: BooleanLiteralExpr, arg: Void): NodeInfo = {
    NodeInfo(token = Some(booleanLiteralExpr.getValue.toString), isBlockRoot = false)
  }

  override def visit(stringLiteralExpr: StringLiteralExpr, arg: Void): NodeInfo = {
    NodeInfo(token = Some(stringLiteralExpr.getValue), isBlockRoot = false)
  }

  override def visit(integerLiteralExpr: IntegerLiteralExpr, arg: Void): NodeInfo = {
    NodeInfo(token = Some(integerLiteralExpr.getValue), isBlockRoot = false)
  }

  override def visit(doubleLiteralExpr: DoubleLiteralExpr, arg: Void): NodeInfo = {
    NodeInfo(token = Some(doubleLiteralExpr.getValue), isBlockRoot = false)
  }

  override def visit(classOrInterfaceDeclaration: ClassOrInterfaceDeclaration, arg: Void): NodeInfo = {
    NodeInfo(token = Some(classOrInterfaceDeclaration.getName.toString()), isBlockRoot = false)
  }
}
