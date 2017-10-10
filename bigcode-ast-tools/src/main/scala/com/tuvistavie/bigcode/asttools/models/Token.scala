package com.tuvistavie.bigcode.asttools.models

case class Token(tokenType: String, value: Option[String] = None) {
  def label: String = value match {
    case Some(v) => v
    case None => tokenType
  }

  def metaType: String = tokenType match {
    case t if t.endsWith("Stmt") || t.endsWith("Statement") => "Stmt"
    case t if t.endsWith("Expr") || t.endsWith("Expression") => "Expr"
    case t if t.startsWith("Literal") || t.endsWith("Literal") => "Literal"
    case t if t.endsWith("Type") => "Type"
    case t if t.endsWith("Declaration") => "Declaration"
    case _ => "Other"
  }
}

