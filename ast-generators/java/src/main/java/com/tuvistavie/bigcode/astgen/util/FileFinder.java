package com.tuvistavie.bigcode.astgen.util;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class FileFinder {
    public static class Result {
        private Set<Path> files;
        private Path root;
        public Result(Set<Path> files, Path root) {
            this.files = files;
            this.root = root;
        }
        public Set<Path> getFiles() {
            return this.files;
        }
        public Path getRoot() {
            return this.root;
        }
    }

    public static Result findFiles(Path path) throws IOException {
        return findFiles(path.toString());
    }

    public static Result findFiles(String files) throws IOException {
        // NOTE: split with look ahead: "/foo/bar/**/*.java" -> ["/foo/bar/", "**/*.java"]
        String[] pathAndGlob = files.split("(?=[*{@!])", 2);
        Path root = Paths.get(pathAndGlob[0]);
        if (pathAndGlob.length == 1) {
            return findFiles(root, path -> true);
        } else {
            String glob = "glob:" + Paths.get(root.toString(), pathAndGlob[1]);
            PathMatcher matcher = FileSystems.getDefault().getPathMatcher(glob);
            return findFiles(root, matcher::matches);
        }
    }

    public static Result findFiles(Path root, Predicate<Path> condition) throws IOException {
        final Set<Path> paths = new HashSet<>();
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                if (condition.test(path)) {
                    paths.add(path);
                }
                return super.visitFile(path, basicFileAttributes);
            }
            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
        return new Result(paths, root);
    }
}
