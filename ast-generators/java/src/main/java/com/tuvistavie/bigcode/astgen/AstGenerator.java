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

public class AstGenerator {
    private Logger logger = LoggerFactory.getLogger(AstGenerator.class);
    private ObjectMapper mapper = new ObjectMapper();

    public List<Map<String, Object>> parseFile(Path filepath) throws IOException {
        CompilationUnit cu = JavaParser.parse(filepath);
        JsonVisitor visitor = new JsonVisitor();
        List<Map<String, Object>> astNodes = new ArrayList<>();
        cu.accept(visitor, astNodes);
        return astNodes;
    }

    public void processAllFiles(Path pattern, Path output) throws IOException {
        FileFinder.Result filesResult = FileFinder.findFiles(pattern);

        String jsonOutput = Paths.get(output.toString(), "asts.json").toString();
        String filesOutput = Paths.get(output.toString(), "files.txt").toString();

        try(PrintWriter jsonWriter = new PrintWriter(jsonOutput, "UTF-8");
            PrintWriter astWriter = new PrintWriter(filesOutput, "UTF-8")) {

            filesResult.getFiles().parallelStream().forEach(file -> {
                try {
                    List<Map<String, Object>> parsed = parseFile(file);

                    String jsonAST = mapper.writeValueAsString(parsed);
                    Path relativePath = filesResult.getRoot().relativize(file);

                    synchronized(this) {
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
