package com.tuvistavie.bigcode.astgen;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

public class Cli {
    public static void main(String[] args) {
        AstGenerator.Options options = new AstGenerator.Options();
        CmdLineParser cmd = new CmdLineParser(options);
        try {
            cmd.parseArgument(args);
            if (options.help) {
                showUsage(cmd, System.out);
                return;
            }
            AstGenerator.processAllFiles(options.getFilesPath(), options.getOutputPath(), options);
        } catch (IOException e) {
            System.out.println("failed to process files: " + e.getMessage());
            System.exit(1);
        } catch (CmdLineException e) {
            System.out.println("failed to parse arguments: " + e.getMessage());
            showUsage(cmd, System.err);
            System.exit(1);
        }
    }

    private static void showUsage(CmdLineParser cmd, OutputStream out) {
        PrintWriter pw = new PrintWriter(out, true);
        pw.println("usage: bigcode-astgen-java -f FILES -o OUTPUT [options]\nOptions:");
        cmd.printUsage(out);
    }
}
