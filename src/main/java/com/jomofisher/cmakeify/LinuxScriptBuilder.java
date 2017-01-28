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

    private static File getRootBuildFolder(File workingDirectory) {
        return new File(workingDirectory, "build");
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
    ScriptBuilder deleteBuildFolder(File workingFolder) {
        return append("rm -rf %s", getRootBuildFolder(workingFolder));
    }

    @Override
    ScriptBuilder download(RemoteArchive remote) {
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
            RemoteArchive cmakeRemote,
            String ndkVersion,
            RemoteArchive ndkRemote,
            String platform,
            String abi,
            boolean multipleCMake,
            boolean multipleNDK,
            boolean multiplePlatforms) {
        String cmakeExe = String.format("%s/%s/bin/cmake", TOOLS_FOLDER,
            cmakeRemote.linux.unpackroot);
        File outputFolder = new File(getRootBuildFolder(workingDirectory), "Android");
        if (multipleCMake) {
            outputFolder = new File(outputFolder, "cmake-" + cmakeVersion);
        }
        if (multipleNDK) {
            outputFolder = new File(outputFolder, ndkVersion);
        }
        if (multiplePlatforms) {
            outputFolder = new File(outputFolder, "android-" + platform);
        }

        File buildFolder = new File(outputFolder, "cmake-generated-files");
        String ndkFolder = String.format("%s/%s", TOOLS_FOLDER, ndkRemote.linux.unpackroot);
        File archFolder = new File(String.format("%s/platforms/android-%s/arch-%s",
            new File(ndkFolder).getAbsolutePath(), platform, Abi.getByName(abi).getArchitecture()));
        append("if [ -d '%s' ]; then", archFolder);
        append("  echo Building to %s\n", outputFolder);
        append("  mkdir --parents %s/redist/lib", outputFolder.getAbsolutePath());
        append("  mkdir --parents %s/redist/include", outputFolder.getAbsolutePath());

        append(String.format(
                "  %s \\\n" +
                "   -H%s \\\n" +
                "   -B%s \\\n" +
                "   -DCMAKE_ANDROID_NDK_TOOLCHAIN_VERSION=4.9 \\\n" +
                "   -DCMAKE_ANDROID_NDK_TOOLCHAIN_DEBUG=1 \\\n" +
                "   -DCMAKE_SYSTEM_NAME=Android \\\n" +
                "   -DCMAKE_SYSTEM_VERSION=%s \\\n" +
                "   -DCMAKEIFY_REDIST_INCLUDE_DIRECTORY=%s/redist/include \\\n" +
                "   -DCMAKE_LIBRARY_OUTPUT_DIRECTORY=%s/redist/lib/%s \\\n" +
                "   -DCMAKE_ANDROID_STL_TYPE=gnustl_static \\\n" +
                "   -DCMAKE_ANDROID_NDK=%s \\\n" +
                "   -DCMAKE_ANDROID_ARCH_ABI=%s \n",
                cmakeExe, workingDirectory, buildFolder, platform,
            outputFolder.getAbsolutePath(),
            outputFolder.getAbsolutePath(),
            abi, new File(ndkFolder).getAbsolutePath(), abi));

        append("  rc=$?; if [[ $rc != 0 ]]; then exit $rc; fi");
        append(String.format("  %s --build %s", cmakeExe, buildFolder));
        append("fi");

        return this;
    }

    @Override
    ScriptBuilder cmakeLinux(
            File workingDirectory,
            String cmakeVersion,
            RemoteArchive cmakeRemote,
            Toolset toolset,
            boolean multipleCMake,
            boolean multipleGcc) {
        String cmakeExe = String.format("%s/%s/bin/cmake", TOOLS_FOLDER,
            cmakeRemote.linux.unpackroot);
        File outputFolder = new File(getRootBuildFolder(workingDirectory), "Linux");
        if (multipleCMake) {
            outputFolder = new File(outputFolder,  "cmake-" + cmakeVersion);
        }
        if (multipleGcc) {
            outputFolder = new File(outputFolder, toolset.c);
        }
        File buildFolder = new File(outputFolder, "cmake-generated-files");
        append("echo Building to %s\n", outputFolder);
        append("mkdir --parents %s/redist/bin", outputFolder.getAbsolutePath());
        append("mkdir --parents %s/redist/include", outputFolder.getAbsolutePath());

        append(String.format(
                "%s \\\n" +
                "   -H%s \\\n" +
                "   -B%s \\\n" +
                "   -DCMAKEIFY_REDIST_INCLUDE_DIRECTORY=%s/redist/include \\\n" +
                "   -DCMAKE_LIBRARY_OUTPUT_DIRECTORY=%s/redist/lib \\\n" +
                "   -DCMAKE_SYSTEM_NAME=Linux \\\n" +
                "   -DCMAKE_C_COMPILER=%s \\\n" +
                "   -DCMAKE_CXX_COMPILER=%s",
                cmakeExe, workingDirectory, buildFolder,
            outputFolder.getAbsolutePath(),
            outputFolder.getAbsolutePath(),
            toolset.c, toolset.cxx));

        append("rc=$?; if [[ $rc != 0 ]]; then exit $rc; fi");
        append(String.format("%s --build %s", cmakeExe, buildFolder));

        return this;
    }

    @Override
    public String toString() {
        return script.toString();
    }
}
