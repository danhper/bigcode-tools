package com.tuvistavie.bigcode.astgen.util;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class FileFinderTest {
    private Path root = Paths.get(getClass().getResource("/fixtures/dummy_project").getPath());

    @Test
    public void testFindFilesWithNoPredicate() throws IOException {
        Set<String> expected = new HashSet<>(Arrays.asList("Main.java", "Main.class", "MyClass.java", "MainTest.java", "MyClassTest.java"));
        FileFinder.Result result = FileFinder.findFiles(root);
        Set<String> actual = pathsToFilename(result.getFiles());
        assertEquals(expected, actual);
        assertEquals(root, result.getRoot());
    }

    @Test
    public void testFindFilesWithExtension() throws IOException {
        Set<String> expected = new HashSet<>(Arrays.asList("Main.java", "MyClass.java", "MainTest.java", "MyClassTest.java"));
        FileFinder.Result result = FileFinder.findFiles(root, p -> p.toString().endsWith("java"));
        Set<String> actual = pathsToFilename(result.getFiles());
        assertEquals(expected, actual);
        assertEquals(root, result.getRoot());
    }

    @Test
    public void testFindFilesWithRecursiveGlob() throws IOException {
        Set<String> expected = new HashSet<>(Arrays.asList("Main.java", "MyClass.java", "MainTest.java", "MyClassTest.java"));
        FileFinder.Result result = FileFinder.findFiles(Paths.get(root.toString(), "**/*.java"));
        Set<String> actual = pathsToFilename(result.getFiles());
        assertEquals(expected, actual);
        assertEquals(root, result.getRoot());
    }

    @Test
    public void testEmptyFindFilesWithNonRecursiveGlob() throws IOException {
        Set<String> expected = new HashSet<>();
        FileFinder.Result result = FileFinder.findFiles(Paths.get(root.toString(), "*.java"));
        Set<String> actual = pathsToFilename(result.getFiles());
        assertEquals(expected, actual);
        assertEquals(root, result.getRoot());
    }
    @Test
    public void testFindFilesWithNonRecursiveGlob() throws IOException {
        Set<String> expected = new HashSet<>(Arrays.asList("Main.java", "MyClass.java"));
        Path srcRoot = Paths.get(root.toString(), "src", "main", "java", "my", "package");
        FileFinder.Result result = FileFinder.findFiles(Paths.get(srcRoot.toString(), "*.java"));
        Set<String> actual = pathsToFilename(result.getFiles());
        assertEquals(expected, actual);
        assertEquals(srcRoot, result.getRoot());
    }

    private Set<String> pathsToFilename(Set<Path> paths) {
        return paths.stream().map(path -> path.getFileName().toString()).collect(Collectors.toSet());
    }
}
