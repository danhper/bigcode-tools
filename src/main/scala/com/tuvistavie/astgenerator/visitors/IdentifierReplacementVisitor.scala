package com.tuvistavie.astgenerator.visitors

import com.github.javaparser.ast.`type`.{ClassOrInterfaceType, TypeParameter}
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.expr._
import com.github.javaparser.ast.visitor.VoidVisitorAdapter

object IdentifierReplacementVisitor {
  val idToken = "<id>"
  val zeroToken = "<zero>"
  val nonZeroToken = "<non-zero>"
  val typeToken = "<type>"
  val stringToken = "<string>"
}

class IdentifierReplacementVisitor extends VoidVisitorAdapter[Void] {

  override def visit(name: Name, arg: Void): Unit = {
    name.setIdentifier(IdentifierReplacementVisitor.idToken)
    if (name.getQualifier.isPresent) {
      name.setQualifier(new Name(IdentifierReplacementVisitor.idToken))
    }
    super.visit(name, arg)
  }

  override def visit(name: NameExpr, arg: Void): Unit = {
    name.setName(IdentifierReplacementVisitor.idToken)
    super.visit(name, arg)
  }

  override def visit(name: SimpleName, arg: Void): Unit = {
    if (!name.getIdentifier.equals(IdentifierReplacementVisitor.typeToken)) {
      name.setIdentifier(IdentifierReplacementVisitor.idToken)
    }
    super.visit(name, arg)
  }

  override def visit(clazz: ClassOrInterfaceType, arg: Void): Unit = {
    clazz.setName(IdentifierReplacementVisitor.typeToken)
    super.visit(clazz, arg)
  }

  override def visit(expr: MethodCallExpr, arg: Void): Unit = {
    expr.setName(IdentifierReplacementVisitor.idToken)
    super.visit(expr, arg)
  }

  override def visit(expr: IntegerLiteralExpr, arg: Void): Unit = {
    if (expr.getValue == "0") {
      expr.setValue(IdentifierReplacementVisitor.zeroToken)
    } else {
      expr.setValue(IdentifierReplacementVisitor.nonZeroToken)
    }
    super.visit(expr, arg)
  }

  override def visit(expr: DoubleLiteralExpr, arg: Void): Unit = {
    if (expr.getValue == "0" || expr.getValue == "0.0") {
      expr.setValue(IdentifierReplacementVisitor.zeroToken)
    } else {
      expr.setValue(IdentifierReplacementVisitor.nonZeroToken)
    }
    super.visit(expr, arg)
  }

  override def visit(expr: StringLiteralExpr, arg: Void): Unit = {
    expr.setValue(IdentifierReplacementVisitor.stringToken)
    super.visit(expr, arg)
  }

  override def visit(expr: FieldAccessExpr, arg: Void): Unit = {
    expr.setName(IdentifierReplacementVisitor.idToken)
    super.visit(expr, arg)
  }

  override def visit(expr: TypeParameter, arg: Void): Unit = {
    expr.setName(IdentifierReplacementVisitor.typeToken)
    super.visit(expr, arg)
  }

  override def visit(decl: ClassOrInterfaceDeclaration, arg: Void): Unit = {
    decl.setName(IdentifierReplacementVisitor.typeToken)
    super.visit(decl, arg)
  }
}
