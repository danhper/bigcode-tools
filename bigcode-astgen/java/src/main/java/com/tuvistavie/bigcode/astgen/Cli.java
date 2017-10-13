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
        Path output = Paths.get(cmd.getOptionValue("output"));

        try {
            int minNodes = getIntOption(cmd, "min-nodes", 20);
            int maxNodes = getIntOption(cmd, "max-nodes", 30000);
            AstGenerator.Options processOptions = new AstGenerator.Options(minNodes, maxNodes);

            AstGenerator.processAllFiles(files, output, processOptions);
        } catch (IOException e) {
            System.out.println("failed to process files: " + e.getMessage());
            System.exit(1);
        } catch (ParseException e) {
            System.out.println("failed to parse arguments: " + e.getMessage());
            System.exit(1);
        }
    }

    private static int getIntOption(CommandLine cmd, String option, int defaultValue) throws ParseException {
        Object parsedOption = cmd.getParsedOptionValue(option);
        if (parsedOption == null) {
            return defaultValue;
        }
        return ((Number)parsedOption).intValue();
    }

    private static Options createOptions() {
        Options options = new Options();
        Option fileOption = new Option("f", "files", true, "Glob pattern of files to parse");
        fileOption.setRequired(true);
        options.addOption(fileOption);

        Option outputOption = new Option("o", "output", true, "Directory where to put the results");
        outputOption.setRequired(true);
        options.addOption(outputOption);

        Option minNodesOption = new Option(null, "min-nodes", true, "Minimum number of nodes");
        outputOption.setType(Number.class);
        options.addOption(minNodesOption);

        Option maxNodesOption = new Option(null, "max-nodes", true, "Maximum number of nodes");
        outputOption.setType(Number.class);
        options.addOption(maxNodesOption);

        return options;
    }
}
