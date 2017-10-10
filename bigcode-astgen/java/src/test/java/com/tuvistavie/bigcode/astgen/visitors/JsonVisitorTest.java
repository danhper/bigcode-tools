package com.tuvistavie.bigcode.astgen.visitors;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.WhileStmt;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class JsonVisitorTest {
    private JsonVisitor visitor = new JsonVisitor();

    @Test
    public void testVisitSimpleName() {
        SimpleName node = new SimpleName("identifier");
        List<Map<String, Object>> nodes = visitNode(node);
        assertEquals(1, nodes.size());
        assertEquals("identifier", nodes.get(0).get(JsonVisitor.valueKey));
        assertEquals("SimpleName", nodes.get(0).get(JsonVisitor.typeKey));
    }

    @Test
    public void testVisitName() {
        Name qualifier = new Name("qualifier");
        Name node = new Name(qualifier,"name");
        List<Map<String, Object>> nodes = visitNode(node);
        assertEquals(2, nodes.size());
        assertEquals("name", nodes.get(0).get(JsonVisitor.valueKey));
        assertEquals("Name", nodes.get(0).get(JsonVisitor.typeKey));
        assertEquals("qualifier", nodes.get(1).get(JsonVisitor.valueKey));
    }

    @Test
    public void testVisitBinaryExpr() {
        IntegerLiteralExpr left = new IntegerLiteralExpr(4);
        DoubleLiteralExpr right = new DoubleLiteralExpr(2.5);
        BinaryExpr node = new BinaryExpr(left, right, BinaryExpr.Operator.PLUS);
        List<Map<String, Object>> nodes = visitNode(node);
        assertEquals(3, nodes.size());
        assertEquals("BinaryExpr", nodes.get(0).get(JsonVisitor.typeKey));
        assertEquals("+", nodes.get(0).get(JsonVisitor.valueKey));

        assertEquals("4", nodes.get(1).get(JsonVisitor.valueKey));
        assertEquals("IntegerLiteralExpr", nodes.get(1).get(JsonVisitor.typeKey));

        assertEquals("2.5", nodes.get(2).get(JsonVisitor.valueKey));
        assertEquals("DoubleLiteralExpr", nodes.get(2).get(JsonVisitor.typeKey));
    }

    @Test
    public void testWhileStmt() {
        Expression condition = new BinaryExpr(
                new NameExpr(new SimpleName("a")),
                new IntegerLiteralExpr(0),
                BinaryExpr.Operator.GREATER);

        NodeList<Statement> bodyStmts = new NodeList<>();
        NodeList<Expression> args = new NodeList<>();
        args.add(new NameExpr(new SimpleName("a")));
        MethodCallExpr expr = new MethodCallExpr(null, new SimpleName("myMethod"), new NodeList<>(args));
        bodyStmts.add(new ExpressionStmt(expr));

        BlockStmt body = new BlockStmt(bodyStmts);
        WhileStmt node = new WhileStmt(condition, body);

        List<Map<String, Object>> nodes = visitNode(node);
        assertEquals(11, nodes.size());

        assertEquals("WhileStmt", nodes.get(0).get(JsonVisitor.typeKey));
        assertEquals(">", nodes.get(1).get(JsonVisitor.valueKey));
        assertEquals("MethodCallExpr", nodes.get(7).get(JsonVisitor.typeKey));
        assertEquals("SimpleName", nodes.get(8).get(JsonVisitor.typeKey));
        assertEquals("myMethod", nodes.get(8).get(JsonVisitor.valueKey));
    }

    private List<Map<String, Object>> visitNode(Node node) {
        List<Map<String, Object>> nodes = new ArrayList<>();
        node.accept(visitor, nodes);
        return nodes;
    }
}
