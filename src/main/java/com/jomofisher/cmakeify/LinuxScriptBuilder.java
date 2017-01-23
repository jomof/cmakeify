package com.jomofisher.cmakeify;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Collection;

public class LinuxScriptBuilder  extends ScriptBuilder {
    final private static String TOOLS_FOLDER = ".cmakeify/tools";
    final private static String DOWNLOADS_FOLDER = ".cmakeify/downloads";
    final private StringBuilder script = new StringBuilder();

    private LinuxScriptBuilder append(String format, Object... args) {
        script.append(String.format(format + "\n", args));
        return this;
    }

    @Override
    ScriptBuilder createToolsFolder() {
        return append("mkdir --parents %s/", TOOLS_FOLDER);
    }

    @Override
    ScriptBuilder createDownloadsFolder() {
        return append("mkdir --parents %s/", DOWNLOADS_FOLDER);
    }

    @Override
    ScriptBuilder downloadCMake(CMakeVersion version) {
        ArchiveInfo archive = new ArchiveInfo(version.linux);
        return append(archive.downloadToFolder(DOWNLOADS_FOLDER))
              .append(archive.uncompressToFolder(DOWNLOADS_FOLDER, TOOLS_FOLDER));
              //.append("CMAKEIFY_CMAKE_FOLDER=%s/%s", TOOLS_FOLDER, archive.baseName);
              //.append("$CMAKEIFY_CMAKE_FOLDER/bin/cmake --version");
    }

    @Override
    File writeToShellScript() {
        BufferedWriter writer = null;
        File file = new File(".cmakeify/build.sh");
        file.getAbsoluteFile().mkdirs();
        file.delete();
        try {
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(script.toString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // Close the writer regardless of what happens...
                writer.close();
            } catch (Exception e) {
            }
        }
        return file;
    }

    @Override
    ScriptBuilder checkForCompilers(Collection<String> compilers) {
        for (String compiler : compilers) {
            append("if [[ -z \"$(which %s)\" ]]; then", compiler);
            append("  echo Missing %s. Please install.", compiler);
            append("  exit 100");
            append("fi");
        }
        return this;
    }

    @Override
    ScriptBuilder cmake(File workingDirectory, CMakeVersion version) {
        ArchiveInfo archive = new ArchiveInfo(version.linux);
        String cmakeExe = String.format("%s/%s/bin/cmake", TOOLS_FOLDER, archive.baseName);
        String outputFolder = String.format("%s/%s", workingDirectory, version.tag);
        append(String.format(
                "echo %s \\\n" +
                "   -B%s/build-files \\\n" +
                "   -DCMAKE_ARCHIVE_OUTPUT_DIRECTORY=%s/bin \\\n" +
                "   -DCMAKE_LIBRARY_OUTPUT_DIRECTORY=%s/bin \\\n" +
                "   -DCMAKE_RUNTIME_OUTPUT_DIRECTORY=%s/bin \\\n" +
                "   -DCMAKE_SYSTEM_NAME=linux \\\n" +
                "   -DCMAKE_C_COMPILER=gcc \\\n" +
                "   -DCMAKE_CXX_COMPILER=g++",
                cmakeExe, outputFolder, outputFolder, outputFolder, outputFolder));

        append(String.format(
                "%s \\\n" +
                "   -B%s/build-files \\\n" +
                "   -DCMAKE_ARCHIVE_OUTPUT_DIRECTORY=%s/bin \\\n" +
                "   -DCMAKE_LIBRARY_OUTPUT_DIRECTORY=%s/bin \\\n" +
                "   -DCMAKE_RUNTIME_OUTPUT_DIRECTORY=%s/bin \\\n" +
                "   -DCMAKE_SYSTEM_NAME=linux \\\n" +
                "   -DCMAKE_C_COMPILER=gcc \\\n" +
                "   -DCMAKE_CXX_COMPILER=g++",
                cmakeExe, outputFolder, outputFolder, outputFolder, outputFolder));
        return this;
    }

    @Override
    public String toString() {
        return script.toString();
    }
}
