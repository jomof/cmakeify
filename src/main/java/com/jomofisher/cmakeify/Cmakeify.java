package com.jomofisher.cmakeify;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.*;

public class Cmakeify {
    private PrintStream out = System.out;
    private File workingFolder = new File(".");
    private Configuration config = null;

    Cmakeify(PrintStream out) {
        this.out = out;
    }

    void go(String [] args) throws IOException {
        if (handleVersion(args)) return;
        handleWorkingFolder(args);
        if (!handleReadConfig(args)) return;
        if (handleDump(args)) return;
    }

    private boolean handleDump(String[] args) {
        for (int i = 0; i < args.length; ++i) {
            if (args[i].equals("--dump") || args[i].equals("-d")) {
                out.printf("target: ");
                for (int j = 0; j < config.target.length; ++j) {
                    out.print(config.target[j] + " ");
                }
                out.printf("\n");

                out.printf("compiler: ");
                for (int j = 0; j < config.compiler.length; ++j) {
                    out.print(config.compiler[j] + " ");
                }
                out.printf("\n");
                return true;
            }
        }
        return false;
    }

    private boolean handleReadConfig(String[] args) throws IOException {
        File config = new File(workingFolder, ".cmakeify.yml");
        if (!config.exists()) {
            out.printf("cmakeify expected a configuration file at %s\n", config.getCanonicalFile());
            return false;
        }

        Yaml yaml = new Yaml(new Constructor(Configuration.class));
        this.config = (Configuration)yaml.load(new FileInputStream(config));
        return true;
    }

    private void handleWorkingFolder(String[] args) {
        boolean takeNext = false;
        for (int i = 0; i < args.length; ++i) {
            if (takeNext) {
                this.workingFolder = new File(args[i]);
                takeNext = false;
            } else if (args[i].equals("--working-folder") || args[i].equals("-wf")) {
                takeNext = true;
            }
        }
    }

    private boolean handleVersion(String[] args) {
        if (args.length != 1 || !args[0].equals("--version")) {
            return false;
        }

        out.printf("cmakeify %s\n", BuildInfo.PROJECT_VERSION);
        return true;
    }

    public static void main(String [] args) throws IOException {
        new Cmakeify(System.out).go(args);
    }
}