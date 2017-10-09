package com.tuvistavie.bigcode.astgen;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuvistavie.bigcode.astgen.visitors.JsonVisitor;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class AstGeneratorTest {
    private Path root = Paths.get(getClass().getResource("/fixtures/dummy_project").getPath());
    private Path srcRoot = Paths.get(root.toString(), "src", "main", "java", "my", "package");

    private Path tempdir;

    @Before
    public void setUp() throws IOException {
        tempdir = Files.createTempDirectory("java-bigcode-ast");
    }

    @Test
    public void testParseSimpleFile() throws IOException {
        Path path = Paths.get(srcRoot.toString(), "Main.java");
        List<Map<String, Object>> nodes = AstGenerator.parseFile(path);
        assertEquals("CompilationUnit", nodes.get(0).get(JsonVisitor.typeKey));
        assertEquals("ClassOrInterfaceDeclaration", nodes.get(1).get(JsonVisitor.typeKey));
        assertEquals("SimpleName", nodes.get(2).get(JsonVisitor.typeKey));
        assertEquals("Main", nodes.get(2).get(JsonVisitor.valueKey));
    }

    @Test
    public void testParseFile() throws IOException {
        Path path = Paths.get(srcRoot.toString(), "MyClass.java");
        List<Map<String, Object>> nodes = AstGenerator.parseFile(path);
        assertEquals("CompilationUnit", nodes.get(0).get(JsonVisitor.typeKey));
    }


    @Test
    public void testProcessAllFiles() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        Path pattern = Paths.get(srcRoot.toString(), "*.java");
        AstGenerator.processAllFiles(pattern, tempdir);
        Set<Path> generatedFiles = Files.list(tempdir).map(Path::getFileName).collect(Collectors.toSet());
        Set<Path> expected = new HashSet<>(Arrays.asList(Paths.get("asts.json"), Paths.get("files.txt")));
        assertEquals(expected, generatedFiles);
        List<String> astLines = Files.readAllLines(Paths.get(tempdir.toString(), "asts.json"));
        List<String> fileLines = Files.readAllLines(Paths.get(tempdir.toString(), "files.txt"));

        assertEquals(2, astLines.size());
        assertEquals(2, fileLines.size());
        assertEquals(new HashSet<>(Arrays.asList("Main.java", "MyClass.java")), new HashSet<>(fileLines));

        for (int i = 0; i < fileLines.size(); i++) {
            String file = fileLines.get(i);
            String className = file.substring(0, file.lastIndexOf("."));
            JsonNode ast = mapper.readTree(astLines.get(i));
            assertEquals(className, ast.get(2).get("value").asText());
        }
    }
}
