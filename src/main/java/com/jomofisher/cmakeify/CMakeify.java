package com.jomofisher.cmakeify;

import com.jomofisher.cmakeify.model.CMakeifyYml;
import com.jomofisher.cmakeify.model.OS;
import com.jomofisher.cmakeify.model.RemoteArchive;
import com.jomofisher.cmakeify.model.Toolset;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

import static com.jomofisher.cmakeify.model.OS.linux;

public class CMakeify {
    private PrintStream out = System.out;
    private File workingFolder = new File(".");
    private CMakeifyYml config = null;
    private String targetGroupId = "";
    private String targetArtifactId = "";
    private String targetVersion = "";
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

    public static void main(String[] args) throws IOException {
        new CMakeify(System.out).go(args);
    }

    void go(String [] args) throws IOException {
        if (!handleVersion(args)) return;
        handleWorkingFolder(args);
        handleCMakeFlags(args);
        handleGroupId(args);
        handleArtifactId(args);
        handleTargetVersion(args);
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
        ScriptBuilder script = new LinuxScriptBuilder(
                BuildInfo.PROJECT_VERSION,
            workingFolder,
            targetGroupId,
            targetArtifactId,
            targetVersion
        );

        // Map of compilers.
        Set<OS> targetOS = new HashSet<>();
        for (int i = 0; i < config.targets.length; ++i) {
            targetOS.add(config.targets[i]);
        }

        // Check that required compilers are installed
        if (targetOS.contains(linux)) {
            Set<String> compilers = new HashSet<>();
            for (String compiler : config.linux.compilers) {
                Toolset toolset = config.linux.toolsets.get(compiler);
                if (toolset == null) {
                    throw new RuntimeException(
                        String.format("Compiler %s was not recognized in toolsets", compiler));
                }
                compilers.add(toolset.c);
                compilers.add(toolset.cxx);
            }
            script.checkForCompilers(compilers);
        }

        // Create working folders
        script.createEmptyBuildFolder();

        // Download the CMakes we need.
        for (String cmakeVersion : config.cmake.versions) {
            // Download the CMake needed.
            RemoteArchive remote = config.cmake.remotes.get(cmakeVersion);
            if (remote == null) {
                throw new RuntimeException(
                    String.format(
                        "CMake version %s is not known. It doesn't have a remote.", cmakeVersion));
            }
            script.download(remote);
        }

        // Download the NDKs that we need
        if (targetOS.contains(OS.android)) {
            for (String version : config.android.ndk.versions) {
                RemoteArchive remote = config.android.ndk.remotes.get(version);
                if ( remote == null) {
                    throw new RuntimeException(
                            String.format("NDK version %s is not known. It doesn't have a remote.", version));
                }
                script.download(remote);
            }
        }

        // Download the CMakes we need.
        for (OS target : targetOS) {
            script.startBuilding(target);
            for (String cmakeVersion : config.cmake.versions) {
                switch (target) {
                    case android:
                        for (String ndk : config.android.ndk.versions) {
                            RemoteArchive remote = config.android.ndk.remotes.get(ndk);
                            if (remote == null) {
                                throw new RuntimeException(String.format("No remote found for NDK %s", ndk));
                            }
                            for (String platform : config.android.ndk.platforms) {
                                for (String compiler : config.android.ndk.compilers) {
                                    for (String runtime : config.android.ndk.runtimes) {
                                        Map<String, String> flavors = config.android.flavors;
                                        if (flavors == null) {
                                            flavors = new HashMap<>();
                                        }
                                        if (flavors.size() == 0) {
                                            flavors.put("default-flavor", "");
                                        }
                                        for (String flavor : flavors.keySet()) {
                                            out.printf("Building script for %s %s %s %s %s\n",
                                                    flavor, ndk, platform, compiler, runtime);
                                            script.cmakeAndroid(
                                                    cmakeVersion,
                                                    config.cmake.remotes.get(cmakeVersion),
                                                    flavor,
                                                    flavors.get(flavor),
                                                    ndk,
                                                    remote,
                                                    config.includes,
                                                    config.android.lib,
                                                    compiler,
                                                    runtime,
                                                    platform,
                                                    config.android.ndk.abis,
                                                    flavors.size() != 1,
                                                    config.cmake.versions.length != 1,
                                                    config.android.ndk.versions.length != 1,
                                                    config.android.ndk.compilers.length != 1,
                                                    config.android.ndk.runtimes.length != 1,
                                                    config.android.ndk.platforms.length != 1);
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    case linux:
                        for (String compiler : config.linux.compilers) {
                            Toolset toolset = config.linux.toolsets.get(compiler);
                            if (toolset == null) {
                                throw new RuntimeException(
                                        String.format("Compiler %s is not a recognized toolset", compiler));
                            }
                            script.cmakeLinux(
                                    cmakeVersion,
                                    config.cmake.remotes.get(cmakeVersion),
                                    toolset,
                                    config.cmake.versions.length != 1,
                                    config.linux.compilers.length != 1);
                        }
                        break;
                }
            }
        }
        script.buildRedistFiles(workingFolder, config.includes);
        script.uploadBadges();
        script.writeToShellScript();
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
        File config = new File(workingFolder, "cmakeify.yml");
        if (!config.exists()) {
            out.printf("Expected a configuration file at %s\n", config.getCanonicalFile());
            return false;
        }

        Yaml yaml = new Yaml(new Constructor(CMakeifyYml.class));
        this.config = (CMakeifyYml) yaml.load(new FileInputStream(config));
        if (this.config == null) {
            this.config = new CMakeifyYml();
        }
        CMakeifyYmlUtils.validateModel(this.config);
        return true;
    }

    private void handleWorkingFolder(String[] args) throws IOException {
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

    private void handleCMakeFlags(String[] args) throws IOException {
        for (int i = 0; i < args.length; ++i) {
            if (args[i].equals("--cmake-flags") || args[i].equals("-cf")) {
                throw new RuntimeException("--cmake-flags no longer supported");
            }
        }
    }

    private void handleGroupId(String[] args) throws IOException {
        boolean takeNext = false;
        for (int i = 0; i < args.length; ++i) {
            if (takeNext) {
                this.targetGroupId = args[i];
                takeNext = false;
            } else if (args[i].equals("--group-id") || args[i].equals("-gid")) {
                takeNext = true;
            }
        }
    }

    private void handleArtifactId(String[] args) throws IOException {
        boolean takeNext = false;
        for (int i = 0; i < args.length; ++i) {
            if (takeNext) {
                this.targetArtifactId = args[i];
                takeNext = false;
            } else if (args[i].equals("--artifact-id") || args[i].equals("-aid")) {
                takeNext = true;
            }
        }
    }

    private void handleTargetVersion(String[] args) throws IOException {
        boolean takeNext = false;
        for (int i = 0; i < args.length; ++i) {
            if (takeNext) {
                this.targetVersion = args[i];
                takeNext = false;
            } else if (args[i].equals("--target-version") || args[i].equals("-tv")) {
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

    enum OSType {
        Windows, MacOS, Linux, Other
    }
}