package com.tuvistavie.bigcode.astgen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.tuvistavie.bigcode.astgen.util.FileFinder;
import com.tuvistavie.bigcode.astgen.visitors.JsonVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class contains static methods to transform files in their
 * JSON AST representation
 */
public class AstGenerator {
    private static Logger logger = LoggerFactory.getLogger(AstGenerator.class);
    private static ObjectMapper mapper = new ObjectMapper();

    private AstGenerator() {}

    /**
     * Returns a list containing all the nodes of the AST contained by the program in {@code filepath}
     *
     * @param filepath the path of the file to parse
     * @return the list of all the nodes in the AST
     * @throws IOException if the file does not exist
     */
    public static List<Map<String, Object>> parseFile(Path filepath) throws IOException {
        CompilationUnit cu = JavaParser.parse(filepath);
        JsonVisitor visitor = new JsonVisitor();
        List<Map<String, Object>> astNodes = new ArrayList<>();
        cu.accept(visitor, astNodes);
        return astNodes;
    }

    /**
     * Processes all the files matched by {@code pattern}, generate the JSON AST and
     * output the result in the {@code output}
     *
     * @param pattern the pattern to search for files
     * @param output  the path where to save result
     *
     * @throws IOException if the {@code output} does not point to an existing directory
     */
    public static void processAllFiles(Path pattern, Path output) throws IOException {
        FileFinder.Result filesResult = FileFinder.findFiles(pattern);

        String jsonOutput = Paths.get(output.toString(), "asts.json").toString();
        String filesOutput = Paths.get(output.toString(), "files.txt").toString();

        final Object writeLock = new Object();

        try(PrintWriter jsonWriter = new PrintWriter(jsonOutput, "UTF-8");
            PrintWriter astWriter = new PrintWriter(filesOutput, "UTF-8")) {

            filesResult.getFiles().parallelStream().forEach(file -> {
                try {
                    List<Map<String, Object>> parsed = parseFile(file);

                    String jsonAST = mapper.writeValueAsString(parsed);
                    Path relativePath = filesResult.getRoot().relativize(file);

                    synchronized(writeLock) {
                        jsonWriter.println(jsonAST);
                        astWriter.println(relativePath);
                    }
                } catch (Exception e) {
                    logger.error("failed to parse " + file + ": " + e.getMessage());
                }
            });
        }
    }
}
