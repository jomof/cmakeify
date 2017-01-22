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
              .append(archive.uncompressToFolder(DOWNLOADS_FOLDER, TOOLS_FOLDER))
              .append("CMAKEIFY_CMAKE_FOLDER=%s/%s", TOOLS_FOLDER, archive.baseName)
              .append("$CMAKEIFY_CMAKE_FOLDER/bin/cmake --version");
    }

    @Override
    File writeToShellScript(File workingFolder) {
        BufferedWriter writer = null;
        File file = new File(workingFolder, ".cmakeify/build.sh");
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
    ScriptBuilder checkForCompilers(Collection<String> packages) {
        for (String p : packages) {
            append("if [[ -z \"$(which %s)\" ]]; then", p);
            append("  echo Missing %s. Please install.", p);
            append("  exit 100");
            append("fi");
        }
        return this;
    }

    @Override
    public String toString() {
        return script.toString();
    }
}
