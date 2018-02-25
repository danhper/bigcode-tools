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
            if (options.batch) {
                AstGenerator.processAllFiles(options.getInputPath(), options.getOutputPath(), options);
            } else {
                AstGenerator.processFile(options.getInputPath(), options.getOutputPath(), options.method);
            }
        } catch (IOException e) {
            System.err.println("failed to process " + options.input + ": " + e.getMessage());
            System.exit(1);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            showUsage(cmd, System.err);
            System.exit(1);
        } catch (Exception e) {
            System.err.println("failed to process " + options.input + ": " + e.getMessage());
            System.exit(1);
        }
    }

    private static void showUsage(CmdLineParser cmd, OutputStream out) {
        PrintWriter pw = new PrintWriter(out, true);
        pw.println("usage: bigcode-astgen-java [options] <input>");
        cmd.printUsage(out);
    }
}
