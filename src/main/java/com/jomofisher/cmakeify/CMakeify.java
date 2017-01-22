package com.jomofisher.cmakeify;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.*;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class CMakeify {
    private PrintStream out = System.out;
    private File workingFolder = new File(".");
    private Configuration config = null;
    enum OSType {
        Windows, MacOS, Linux, Other
    }

    private OSType hostOS;

    CMakeify(PrintStream out) {
        this.out = out;
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

    void go(String [] args) throws IOException {
        if (!handleVersion(args)) return;
        handleWorkingFolder(args);
        if (!handleReadConfig(args)) return;
        if (!handleDump(args)) return;
        if (!handleSupportedHostOS(args)) return;
        handleGenerateScript();
    }

    private boolean handleSupportedHostOS(String args[]) {

        // Override host OS if requested
        boolean takeNext = false;
        for (int i = 0; i < args.length; ++i) {
            if (takeNext) {
                switch(args[i]) {
                    case "Windows":
                        hostOS = OSType.Windows;
                        break;
                    case "MacOS":
                        hostOS = OSType.MacOS;
                        break;
                    case "Linux":
                        hostOS = OSType.Linux;
                        break;
                    default:
                        out.printf("host os specified by --host or -h %s not supported by cmakeify\n", args[i]);
                        return false;
                }
                takeNext = false;
            } else if (args[i].equals("--host") || args[i].equals("-h")) {
                takeNext = true;
            }
        }
        if (hostOS != OSType.Linux) {
            out.printf("host OS type %s is not currently supported by cmakeify\n", hostOS);
            return false;
        }
        return true;
    }

    private void handleGenerateScript() {
        ScriptBuilder script =  new LinuxScriptBuilder();

        Set<OS> targetOS = new HashSet<>();
        for (int i = 0; i < config.targets.length; ++i) {
            targetOS.add(config.targets[i]);
        }

        // Check that required compilers are installed
        Set<String> compilers = new HashSet<>();
        for (GccVersion gccVersion : config.gcc.versions) {
            if (!targetOS.contains(gccVersion.target)) {
                continue;
            }
            compilers.add(gccVersion.c);
            compilers.add(gccVersion.cxx);
        }
        script.checkForCompilers(compilers);

        // Create working folders
        script.createToolsFolder();
        script.createDownloadsFolder();

        // Download the CMakes we need.
        for (CMakeVersion cmakeVersion : config.cmake.versions) {
            // Download the CMake needed.
            script.downloadCMake(cmakeVersion);
        }

        script.writeToShellScript(workingFolder);
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
            out.printf("# Expected a configuration file at %s\n", config.getCanonicalFile());
            return false;
        }

        Yaml yaml = new Yaml(new Constructor(Configuration.class));
        this.config = (Configuration)yaml.load(new FileInputStream(config));
        if (this.config == null) {
            this.config = new Configuration();
        }
        return true;
    }

    private void handleWorkingFolder(String[] args) throws IOException {
        boolean takeNext = false;
        for (int i = 0; i < args.length; ++i) {
            if (takeNext) {
                this.workingFolder = new File(args[i]);
                out.printf("# Setting working folder to %s\n", this.workingFolder.getCanonicalFile());
                takeNext = false;
            } else if (args[i].equals("--working-folder") || args[i].equals("-wf")) {
                takeNext = true;
            } else {
                out.printf("# Working folder considered and rejected %s\n", args[i]);
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