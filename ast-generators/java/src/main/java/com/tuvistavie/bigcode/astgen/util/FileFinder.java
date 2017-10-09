package com.tuvistavie.bigcode.astgen.util;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * This class contains utility methods to search for files in a directory
 */
public class FileFinder {
    private FileFinder() {}

    /**
     * This class contain the result of a file search
     */
    public static class Result {
        private Set<Path> files;
        private Path root;
        public Result(Set<Path> files, Path root) {
            this.files = files;
            this.root = root;
        }

        /**
         * Returns all the files found
         * @return the files found
         */
        public Set<Path> getFiles() {
            return this.files;
        }

        /**
         * Returns the root directory of the search
         * @return the root directory of the search
         */
        public Path getRoot() {
            return this.root;
        }
    }


    /**
     * @see FileFinder#findFiles(String)
     */
    public static Result findFiles(Path pattern) throws IOException {
        return findFiles(pattern.toString());
    }

    /**
     * Returns the files matched by the pattern passed in
     * @param pattern a glob pattern to search for files (e.g. "src/**&#47;*.java")
     *                if {@code pattern} is a simple path, all files will be found recursively
     * @return result of the search
     * @throws IOException if the path does not exist
     */
    public static Result findFiles(String pattern) throws IOException {
        // NOTE: split with look ahead: "/foo/bar/**/*.java" -> ["/foo/bar/", "**/*.java"]
        String[] pathAndGlob = pattern.split("(?=[*{@!])", 2);
        Path root = Paths.get(pathAndGlob[0]);
        if (pathAndGlob.length == 1) {
            return findFiles(root, path -> true);
        } else {
            String glob = "glob:" + Paths.get(root.toString(), pathAndGlob[1]);
            PathMatcher matcher = FileSystems.getDefault().getPathMatcher(glob);
            return findFiles(root, matcher::matches);
        }
    }

    /**
     * Returns all the files in {@code root} matching {@code conditon}
     * @param root the root directory where to search files
     * @param condition a predicate to search for files
     * @return all the matched files
     * @throws IOException if {@code root does not exist}
     */
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
