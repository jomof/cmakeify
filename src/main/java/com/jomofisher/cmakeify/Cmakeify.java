package com.jomofisher.cmakeify;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.PrintStream;

public class Cmakeify {
    private PrintStream out = System.out;
    Cmakeify(PrintStream out) {
        this.out = out;
    }

    void go(String [] args) {
        Yaml yaml = new Yaml();
        out.printf("cmakeify %s\n", BuildInfo.PROJECT_VERSION);
    }

    public static void main(String [] args) throws IOException {
        new Cmakeify(System.out).go(args);
    }
}