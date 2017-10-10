package com.tuvistavie.astgenerator.visitors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.*;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;

import java.util.EnumSet;

public class JsonVisitor extends GenericVisitorAdapter<JsonNode, Void> {
    private static String childrenKey = "children";
    private static String annotationsKey = "annotations";
    private static String commentKey = "comment";

    protected void setProperty(ObjectNode node, String key, EnumSet<Modifier> modifiers) {
        node.set(key, JsonNodeFactory.instance.textNode(modifiers.toString()));
    }

    protected void setProperty(ObjectNode node, String key, String text) {
        node.set(key, JsonNodeFactory.instance.textNode(text));
    }

    protected void setProperty(ObjectNode node, String key, Enum<?> e) {
        node.set(key, JsonNodeFactory.instance.textNode(e.toString()));
    }

    protected void setProperty(ObjectNode node, String key, boolean value) {
        node.set(key, JsonNodeFactory.instance.booleanNode(value));
    }

    private ObjectNode enterNode(Node node, Void arg) {
        ObjectNode result = JsonNodeFactory.instance.objectNode();
        setProperty(result, "type", node.getClass().getSimpleName());
        return result;
    }

    @Override
    public JsonNode visit(NodeList n, Void arg) {
        ArrayNode result = JsonNodeFactory.instance.arrayNode();
        ((NodeList<Node>) n).forEach( x -> result.add(x.accept(this, arg)));
        return result;
    }

    public JsonNode visit(AnnotationDeclaration n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        setProperty(result, "modifiers", n.getModifiers());
        ArrayNode children = result.withArray(childrenKey);
        children.add(n.getMembers().accept(this, arg));
        children.add(n.getName().accept(this, arg));
        result.set(annotationsKey, n.getAnnotations().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(AnnotationMemberDeclaration n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        setProperty(result, "modifiers", n.getModifiers());
        ArrayNode children = result.withArray(childrenKey);
        children.add(n.getName().accept(this, arg));
        children.add(n.getType().accept(this, arg));
        result.set(annotationsKey, n.getAnnotations().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(ArrayAccessExpr n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        children.add(n.getIndex().accept(this, arg));
        children.add(n.getName().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(ArrayCreationExpr n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        children.add(n.getElementType().accept(this, arg));
        n.getInitializer().ifPresent( c -> children.add(c.accept(this, arg)));
        children.add(n.getLevels().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(ArrayCreationLevel n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        result.set(annotationsKey, n.getAnnotations().accept(this, arg));
        n.getDimension().ifPresent( c -> children.add(c.accept(this, arg)));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(ArrayInitializerExpr n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        children.add(n.getValues().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(ArrayType n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        children.add(n.getComponentType().accept(this, arg));
        result.set(annotationsKey, n.getAnnotations().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(AssertStmt n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        children.add(n.getCheck().accept(this, arg));
        n.getMessage().ifPresent( c -> children.add(c.accept(this, arg)));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(AssignExpr n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        setProperty(result, "operator", n.getOperator());
        children.add(n.getTarget().accept(this, arg));
        children.add(n.getValue().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(BinaryExpr n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        setProperty(result, "operator", n.getOperator());
        children.add(n.getLeft().accept(this, arg));
        children.add(n.getRight().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(BlockComment n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        setProperty(result, "content", n.getContent());
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(BlockStmt n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        result.set(childrenKey, n.getStatements().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(BooleanLiteralExpr n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        setProperty(result, "value", n.getValue());
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(BreakStmt n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        n.getLabel().ifPresent( c -> children.add(c.accept(this, arg)));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(CastExpr n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        children.add(n.getExpression().accept(this, arg));
        children.add(n.getType().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(CatchClause n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        children.add(n.getBody().accept(this, arg));
        children.add(n.getParameter().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(CharLiteralExpr n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        setProperty(result, "value", n.getValue());
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(ClassExpr n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        children.add(n.getType().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(ClassOrInterfaceDeclaration n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        setProperty(result, "isInterface", n.isInterface());
        setProperty(result, "modifiers", n.getModifiers());
        children.add(n.getExtendedTypes().accept(this, arg));
        children.add(n.getImplementedTypes().accept(this, arg));
        children.add(n.getTypeParameters().accept(this, arg));
        children.add(n.getMembers().accept(this, arg));
        children.add(n.getName().accept(this, arg));
        result.set(annotationsKey, n.getAnnotations().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(ClassOrInterfaceType n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        children.add(n.getName().accept(this, arg));
        n.getScope().ifPresent( c -> children.add(c.accept(this, arg)));
        n.getTypeArguments().ifPresent( c -> children.add(c.accept(this, arg)));
        result.set(annotationsKey, n.getAnnotations().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(CompilationUnit n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        children.add(n.getImports().accept(this, arg));
        n.getPackageDeclaration().ifPresent( c -> children.add(c.accept(this, arg)));
        children.add(n.getTypes().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(ConditionalExpr n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        children.add(n.getCondition().accept(this, arg));
        children.add(n.getElseExpr().accept(this, arg));
        children.add(n.getThenExpr().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(ConstructorDeclaration n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        setProperty(result, "modifiers", n.getModifiers());
        children.add(n.getBody().accept(this, arg));
        children.add(n.getName().accept(this, arg));
        children.add(n.getParameters().accept(this, arg));
        children.add(n.getThrownExceptions().accept(this, arg));
        children.add(n.getTypeParameters().accept(this, arg));
        result.set(annotationsKey, n.getAnnotations().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(ContinueStmt n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        n.getLabel().ifPresent( c -> children.add(c.accept(this, arg)));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(DoStmt n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        children.add(n.getBody().accept(this, arg));
        children.add(n.getCondition().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(DoubleLiteralExpr n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        setProperty(result, "value", n.getValue());
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(EmptyStmt n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(EnclosedExpr n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        children.add(n.getInner().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(EnumConstantDeclaration n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        children.add(n.getArguments().accept(this, arg));
        children.add(n.getClassBody().accept(this, arg));
        children.add(n.getName().accept(this, arg));
        result.set(annotationsKey, n.getAnnotations().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(EnumDeclaration n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        setProperty(result, "modifiers", n.getModifiers());
        children.add(n.getEntries().accept(this, arg));
        children.add(n.getImplementedTypes().accept(this, arg));
        children.add(n.getMembers().accept(this, arg));
        children.add(n.getName().accept(this, arg));
        result.set(annotationsKey, n.getAnnotations().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(ExplicitConstructorInvocationStmt n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        setProperty(result, "isThis", n.isThis());
        children.add(n.getArguments().accept(this, arg));
        n.getExpression().ifPresent( c -> children.add(c.accept(this, arg)));
        n.getTypeArguments().ifPresent( c -> children.add(c.accept(this, arg)));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(ExpressionStmt n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        children.add(n.getExpression().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(FieldAccessExpr n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        children.add(n.getName().accept(this, arg));
        children.add(n.getScope().accept(this, arg));
        n.getTypeArguments().ifPresent( c -> children.add(c.accept(this, arg)));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(FieldDeclaration n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        setProperty(result, "modifiers", n.getModifiers());
        children.add(n.getVariables().accept(this, arg));
        result.set(annotationsKey, n.getAnnotations().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(ForStmt n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        children.add(n.getBody().accept(this, arg));
        n.getCompare().ifPresent( c -> children.add(c.accept(this, arg)));
        children.add(n.getInitialization().accept(this, arg));
        children.add(n.getUpdate().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(ForeachStmt n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        children.add(n.getBody().accept(this, arg));
        children.add(n.getIterable().accept(this, arg));
        children.add(n.getVariable().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(IfStmt n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        children.add(n.getCondition().accept(this, arg));
        n.getElseStmt().ifPresent( c -> children.add(c.accept(this, arg)));
        children.add(n.getThenStmt().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(ImportDeclaration n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        setProperty(result, "isAsterisk", n.isAsterisk());
        setProperty(result, "isStatic", n.isStatic());
        children.add(n.getName().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(InitializerDeclaration n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        setProperty(result, "isStatic", n.isStatic());
        children.add(n.getBody().accept(this, arg));
        result.set(annotationsKey, n.getAnnotations().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(InstanceOfExpr n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        children.add(n.getExpression().accept(this, arg));
        children.add(n.getType().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(IntegerLiteralExpr n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        setProperty(result, "value", n.getValue());
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(IntersectionType n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        children.add(n.getElements().accept(this, arg));
        result.set(annotationsKey, n.getAnnotations().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(JavadocComment n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        setProperty(result, "content", n.getContent());
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(LabeledStmt n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        children.add(n.getLabel().accept(this, arg));
        children.add(n.getStatement().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(LambdaExpr n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        setProperty(result, "isEnclosingParameters", n.isEnclosingParameters());
        children.add(n.getBody().accept(this, arg));
        children.add(n.getParameters().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(LineComment n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        setProperty(result, "content", n.getContent());
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(LocalClassDeclarationStmt n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        children.add(n.getClassDeclaration().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(LongLiteralExpr n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        setProperty(result, "value", n.getValue());
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(MarkerAnnotationExpr n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        children.add(n.getName().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(MemberValuePair n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        children.add(n.getName().accept(this, arg));
        children.add(n.getValue().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(MethodCallExpr n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        children.add(n.getArguments().accept(this, arg));
        children.add(n.getName().accept(this, arg));
        n.getScope().ifPresent( c -> children.add(c.accept(this, arg)));
        n.getTypeArguments().ifPresent( c -> children.add(c.accept(this, arg)));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(MethodDeclaration n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        setProperty(result, "isDefault", n.isDefault());
        setProperty(result, "modifiers", n.getModifiers());
        n.getBody().ifPresent( c -> children.add(c.accept(this, arg)));
        children.add(n.getType().accept(this, arg));
        children.add(n.getName().accept(this, arg));
        children.add(n.getParameters().accept(this, arg));
        children.add(n.getThrownExceptions().accept(this, arg));
        children.add(n.getTypeParameters().accept(this, arg));
        result.set(annotationsKey, n.getAnnotations().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(MethodReferenceExpr n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        setProperty(result, "identifier", n.getIdentifier());
        children.add(n.getScope().accept(this, arg));
        n.getTypeArguments().ifPresent( c -> children.add(c.accept(this, arg)));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(NameExpr n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        children.add(n.getName().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(Name n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        setProperty(result, "identifier", n.getIdentifier());
        n.getQualifier().ifPresent( c -> children.add(c.accept(this, arg)));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(NormalAnnotationExpr n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        children.add(n.getPairs().accept(this, arg));
        children.add(n.getName().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(NullLiteralExpr n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(ObjectCreationExpr n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        n.getAnonymousClassBody().ifPresent( c -> children.add(c.accept(this, arg)));
        children.add(n.getArguments().accept(this, arg));
        n.getScope().ifPresent( c -> children.add(c.accept(this, arg)));
        children.add(n.getType().accept(this, arg));
        n.getTypeArguments().ifPresent( c -> children.add(c.accept(this, arg)));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(PackageDeclaration n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        result.set(annotationsKey, n.getAnnotations().accept(this, arg));
        children.add(n.getName().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(Parameter n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        setProperty(result, "isVarArgs", n.isVarArgs());
        setProperty(result, "modifiers", n.getModifiers());
        result.set(annotationsKey, n.getAnnotations().accept(this, arg));
        children.add(n.getName().accept(this, arg));
        children.add(n.getType().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(PrimitiveType n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        setProperty(result, "type", n.getType());
        result.set(annotationsKey, n.getAnnotations().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(ReturnStmt n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        n.getExpression().ifPresent( c -> children.add(c.accept(this, arg)));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(SimpleName n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        setProperty(result, "identifier", n.getIdentifier());
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(SingleMemberAnnotationExpr n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        children.add(n.getMemberValue().accept(this, arg));
        children.add(n.getName().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(StringLiteralExpr n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        setProperty(result, "value", n.getValue());
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(SuperExpr n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        n.getClassExpr().ifPresent( c -> children.add(c.accept(this, arg)));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(SwitchEntryStmt n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        n.getLabel().ifPresent( c -> children.add(c.accept(this, arg)));
        children.add(n.getStatements().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(SwitchStmt n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        children.add(n.getEntries().accept(this, arg));
        children.add(n.getSelector().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(SynchronizedStmt n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        children.add(n.getBody().accept(this, arg));
        children.add(n.getExpression().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(ThisExpr n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        n.getClassExpr().ifPresent( c -> children.add(c.accept(this, arg)));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(ThrowStmt n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        children.add(n.getExpression().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(TryStmt n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        children.add(n.getCatchClauses().accept(this, arg));
        n.getFinallyBlock().ifPresent( c -> children.add(c.accept(this, arg)));
        children.add(n.getResources().accept(this, arg));
        children.add(n.getTryBlock().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(TypeExpr n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        children.add(n.getType().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(TypeParameter n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        children.add(n.getName().accept(this, arg));
        children.add(n.getTypeBound().accept(this, arg));
        result.set(annotationsKey, n.getAnnotations().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(UnaryExpr n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        setProperty(result, "operator", n.getOperator());
        children.add(n.getExpression().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(UnionType n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        children.add(n.getElements().accept(this, arg));
        result.set(annotationsKey, n.getAnnotations().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(UnknownType n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        result.set(annotationsKey, n.getAnnotations().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(VariableDeclarationExpr n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        setProperty(result, "modifiers", n.getModifiers());
        result.set(annotationsKey, n.getAnnotations().accept(this, arg));
        children.add(n.getVariables().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(VariableDeclarator n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        n.getInitializer().ifPresent( c -> children.add(c.accept(this, arg)));
        children.add(n.getName().accept(this, arg));
        children.add(n.getType().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(VoidType n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        result.set(annotationsKey, n.getAnnotations().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(WhileStmt n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        children.add(n.getBody().accept(this, arg));
        children.add(n.getCondition().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }

    public JsonNode visit(WildcardType n, Void arg) {
        ObjectNode result = enterNode(n, arg);
        ArrayNode children = result.withArray(childrenKey);
        n.getExtendedType().ifPresent( c -> children.add(c.accept(this, arg)));
        n.getSuperType().ifPresent( c -> children.add(c.accept(this, arg)));
        result.set(annotationsKey, n.getAnnotations().accept(this, arg));
        n.getComment().ifPresent( c -> result.set(commentKey, c.accept(this, arg)));
        return result;
    }
}
