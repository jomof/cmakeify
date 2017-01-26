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
    ScriptBuilder download(Remote remote) {
        ArchiveInfo archive = new ArchiveInfo(remote.linux);
        return append(archive.downloadToFolder(DOWNLOADS_FOLDER))
              .append(archive.uncompressToFolder(DOWNLOADS_FOLDER, TOOLS_FOLDER));
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
    ScriptBuilder cmakeAndroid(
            File workingDirectory,
            String cmakeVersion,
            Remote cmakeRemote,
            String ndkVersion,
            Remote ndkRemote,
            String abi,
            boolean multipleCMake,
            boolean multipleNDK) {
        ArchiveInfo cmakeArchive = new ArchiveInfo(cmakeRemote.linux);
        ArchiveInfo ndkArchive = new ArchiveInfo(ndkRemote.linux);
        String cmakeExe = String.format("%s/%s/bin/cmake", TOOLS_FOLDER, cmakeArchive.baseName);
        File outputFolder = new File(workingDirectory, "Android");
        if (multipleCMake) {
            outputFolder = new File(outputFolder, cmakeVersion);
        }
        if (multipleNDK) {
            outputFolder = new File(outputFolder, ndkVersion);
        }
        File buildFolder = new File(outputFolder, "build-files");
        append("mkdir --parents %s", buildFolder);
        append("echo Building to %s\n", outputFolder);
        String ndkFolder = String.format("%s/android-ndk-%s", TOOLS_FOLDER, ndkVersion);
        append("ls %s", ndkFolder);

        append(String.format(
                "%s \\\n" +
                "   -H%s \\\n" +
                "   -B%s \\\n" +
                "   -DCMAKE_ARCHIVE_OUTPUT_DIRECTORY=%s/bin \\\n" +
                "   -DCMAKE_LIBRARY_OUTPUT_DIRECTORY=%s/bin \\\n" +
                "   -DCMAKE_RUNTIME_OUTPUT_DIRECTORY=%s/bin \\\n" +
                "   -DCMAKE_SYSTEM_NAME=Android \\\n" +
                "   -DCMAKE_SYSTEM_VERSION=21 \\\n" +
                "   -DCMAKE_ANDROID_STL_TYPE=gnustl_static \\\n" +
                "   -DCMAKE_ANDROID_NDK=%s \\\n" +
                "   -DCMAKE_ANDROID_ARCH_ABI=%s \n",
                cmakeExe, workingDirectory, buildFolder, outputFolder, outputFolder, outputFolder,
                ndkFolder, abi));

        append(String.format("%s --build %s", cmakeExe, buildFolder));

        return this;
    }

    @Override
    ScriptBuilder cmakeLinux(
            File workingDirectory,
            String cmakeVersion,
            Remote cmakeRemote,
            Toolset toolset,
            boolean multipleCMake,
            boolean multipleGcc) {
        ArchiveInfo archive = new ArchiveInfo(cmakeRemote.linux);
        String cmakeExe = String.format("%s/%s/bin/cmake", TOOLS_FOLDER, archive.baseName);
        File outputFolder = new File(workingDirectory, "Linux");
        if (multipleCMake) {
            outputFolder = new File(outputFolder, cmakeVersion);
        }
        if (multipleGcc) {
            outputFolder = new File(outputFolder, toolset.c);
        }
        File buildFolder = new File(outputFolder, "build-files");
        append("mkdir --parents %s", buildFolder);
        append("echo Building to %s\n", outputFolder);

        append(String.format(
                "%s \\\n" +
                "   -H%s \\\n" +
                "   -B%s \\\n" +
                "   -DCMAKE_ARCHIVE_OUTPUT_DIRECTORY=%s/bin \\\n" +
                "   -DCMAKE_LIBRARY_OUTPUT_DIRECTORY=%s/bin \\\n" +
                "   -DCMAKE_RUNTIME_OUTPUT_DIRECTORY=%s/bin \\\n" +
                "   -DCMAKE_SYSTEM_NAME=Linux \\\n" +
                "   -DCMAKE_C_COMPILER=%s \\\n" +
                "   -DCMAKE_CXX_COMPILER=%s",
                cmakeExe, workingDirectory, buildFolder, outputFolder, outputFolder, outputFolder,
                    toolset.c, toolset.cxx));

        append(String.format("%s --build %s", cmakeExe, buildFolder));

        return this;
    }

    @Override
    public String toString() {
        return script.toString();
    }
}
