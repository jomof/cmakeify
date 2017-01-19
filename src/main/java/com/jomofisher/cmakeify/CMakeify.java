package com.jomofisher.cmakeify;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.*;
import java.util.Locale;

public class CMakeify {
    private PrintStream out = System.out;
    private File workingFolder = new File(".");
    private Configuration config = null;
    enum OSType {
        Windows, MacOS, Linux, Other
    }

    private static OSType hostOS;

    static {
        if (hostOS == null) {
            String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
            if ((OS.indexOf("mac") >= 0) || (OS.indexOf("darwin") >= 0)) {
                hostOS = OSType.MacOS;
            } else if (OS.indexOf("win") >= 0) {
                hostOS = OSType.Windows;
            } else if (OS.indexOf("nux") >= 0) {
                hostOS = OSType.Linux;
            } else {
                hostOS = OSType.Other;
            }
        }
    }

    CMakeify(PrintStream out) {
        this.out = out;
    }

    void go(String [] args) throws IOException {
        if (!handleVersion(args)) return;
        handleWorkingFolder(args);
        if (!handleReadConfig(args)) return;
        if (!handleDump(args)) return;
        if (!handleSupportedHostOS()) return;
        handleGenerateScript();
    }

    private boolean handleSupportedHostOS() {
        if (hostOS != OSType.Linux) {
            out.printf("host OS type %s is not currently supported by cmakeify\n", hostOS);
            return false;
        }
        return true;
    }

    private void handleGenerateScript() {
        StringBuilder script = new StringBuilder();
    }

    private boolean handleDump(String[] args) {
        for (int i = 0; i < args.length; ++i) {
            if (args[i].equals("--dump") || args[i].equals("-d")) {
                out.print(config.toString());
                return false;
            }
        }
        return true;
    }

    private boolean handleReadConfig(String[] args) throws IOException {
        File config = new File(workingFolder, ".cmakeify.yml");
        if (!config.exists()) {
            out.printf("cmakeify expected a configuration file at %s\n", config.getCanonicalFile());
            return false;
        }

        Yaml yaml = new Yaml(new Constructor(Configuration.class));
        this.config = (Configuration)yaml.load(new FileInputStream(config));
        if (this.config == null) {
            this.config = new Configuration();
        }
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
            return true;
        }

        out.printf("cmakeify %s\n", BuildInfo.PROJECT_VERSION);
        return false;
    }

    public static void main(String [] args) throws IOException {
        new CMakeify(System.out).go(args);
    }
}