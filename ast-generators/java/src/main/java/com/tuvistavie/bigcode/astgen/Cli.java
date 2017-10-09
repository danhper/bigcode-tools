package com.tuvistavie.bigcode.astgen;

import org.apache.commons.cli.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Cli {
    public static void main(String[] args) {
        Options options = createOptions();

        CommandLineParser parser = new PosixParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("java-bigcode-ast", options);
            System.exit(1);
            return;
        }

        Path files = Paths.get(cmd.getOptionValue("files"));
        Path outputDir = Paths.get(cmd.getOptionValue("output-dir"));

        try {
            AstGenerator.processAllFiles(files, outputDir);
        } catch (IOException e) {
            System.out.println("failed to process files: " + e.getMessage());
        }
    }

    private static Options createOptions() {
        Options options = new Options();
        Option fileOption = new Option("f", "files", true, "Glob pattern of files to parse");
        fileOption.setRequired(true);
        options.addOption(fileOption);

        Option outputOption = new Option("o", "output-dir", true, "Directory where to put the results");
        outputOption.setRequired(true);
        options.addOption(outputOption);

        return options;
    }
}
