package com.jomofisher.cmakeify;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;

public class LinuxScriptBuilder  extends ScriptBuilder {
    final private static String TOOLS_FOLDER = ".cmakeify/tools";
    final private static String DOWNLOADS_FOLDER = ".cmakeify/downloads";
    final private StringBuilder body = new StringBuilder();
    final private Map<String, String> redistFolderToZip = new HashMap<>();

    private LinuxScriptBuilder body(String format, Object... args) {
        body.append(String.format(format + "\n", args));
        return this;
    }

    private static File getRootBuildFolder(File workingDirectory) {
        return new File(workingDirectory, "build");
    }
    private static File getOutputZipsFolder(File workingDirectory) {
        return new File(getRootBuildFolder(workingDirectory), "zip");
    }

    @Override
    ScriptBuilder createEmptyBuildFolder(File workingFolder) {
        return body("rm -rf %s", getRootBuildFolder(workingFolder))
                .body("mkdir --parents %s", getOutputZipsFolder(workingFolder))
                .body("mkdir --parents %s/", TOOLS_FOLDER)
                .body("mkdir --parents %s/", DOWNLOADS_FOLDER);
    }

    @Override
    ScriptBuilder download(RemoteArchive remote) {
        ArchiveInfo archive = new ArchiveInfo(remote.linux);
        return body(archive.downloadToFolder(DOWNLOADS_FOLDER))
              .body(archive.uncompressToFolder(DOWNLOADS_FOLDER, TOOLS_FOLDER));
    }

    @Override
    File writeToShellScript() {
        BufferedWriter writer = null;
        File file = new File(".cmakeify/build.sh");
        file.getAbsoluteFile().mkdirs();
        file.delete();
        try {
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(body.toString());
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
            body("if [[ -z \"$(which %s)\" ]]; then", compiler);
            body("  echo Missing %s. Please install.", compiler);
            body("  exit 100");
            body("fi");
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
        String zipName = workingDirectory.getAbsoluteFile().getParentFile().getName() + "-android";
        if (multipleCMake) {
            outputFolder = new File(outputFolder, "cmake-" + cmakeVersion);
            zipName += "-cmake-" + cmakeVersion;
        }
        if (multipleNDK) {
            outputFolder = new File(outputFolder, ndkVersion);
            zipName += "-" + ndkVersion;
        }
        if (multiplePlatforms) {
            outputFolder = new File(outputFolder, "android-" + platform);
            zipName += "-platform-" + platform;
        }
        zipName += ".zip";
        File zip = new File(getOutputZipsFolder(workingDirectory), zipName).getAbsoluteFile();

        File buildFolder = new File(outputFolder, "cmake-generated-files");
        String ndkFolder = String.format("%s/%s", TOOLS_FOLDER, ndkRemote.linux.unpackroot);
        File archFolder = new File(String.format("%s/platforms/android-%s/arch-%s",
            new File(ndkFolder).getAbsolutePath(), platform, Abi.getByName(abi).getArchitecture()));
        File redistFolder = new File(outputFolder, "redist").getAbsoluteFile();
        body("if [ -d '%s' ]; then", archFolder);
        body("  echo Building to %s\n", outputFolder);
        body("  mkdir --parents %s/redist/lib", outputFolder.getAbsolutePath());
        body("  mkdir --parents %s/redist/include", outputFolder.getAbsolutePath());

        body(String.format(
                "  %s \\\n" +
                "   -H%s \\\n" +
                "   -B%s \\\n" +
                "   -DCMAKE_ANDROID_NDK_TOOLCHAIN_VERSION=4.9 \\\n" +
                "   -DCMAKE_ANDROID_NDK_TOOLCHAIN_DEBUG=1 \\\n" +
                "   -DCMAKE_SYSTEM_NAME=Android \\\n" +
                "   -DCMAKE_SYSTEM_VERSION=%s \\\n" +
                "   -DCMAKEIFY_REDIST_INCLUDE_DIRECTORY=%s/include \\\n" +
                "   -DCMAKE_LIBRARY_OUTPUT_DIRECTORY=%s/lib/%s \\\n" +
                "   -DCMAKE_ANDROID_STL_TYPE=gnustl_static \\\n" +
                "   -DCMAKE_ANDROID_NDK=%s \\\n" +
                "   -DCMAKE_ANDROID_ARCH_ABI=%s \n",
                cmakeExe, workingDirectory, buildFolder, platform,
            redistFolder, redistFolder, abi, new File(ndkFolder).getAbsolutePath(), abi));
        body(String.format("  %s --build %s", cmakeExe, buildFolder));
        body("  rc=$?; if [[ $rc != 0 ]]; then exit $rc; fi");
        redistFolderToZip.put(redistFolder.getAbsolutePath(), zip.getAbsolutePath());
        body("fi");
        return this;
    }

    @Override
    ScriptBuilder cmakeLinux(
            File workingDirectory,
            String cmakeVersion,
            RemoteArchive cmakeRemote,
            Toolset toolset,
            boolean multipleCMake,
            boolean multipleCompiler) {
        String cmakeExe = String.format("%s/%s/bin/cmake", TOOLS_FOLDER,
            cmakeRemote.linux.unpackroot);
        File outputFolder = new File(getRootBuildFolder(workingDirectory), "Linux");
        String zipName = workingDirectory.getAbsoluteFile().getParentFile().getName() + "-linux";
        if (multipleCMake) {
            outputFolder = new File(outputFolder,  "cmake-" + cmakeVersion);
            zipName += "-cmake-" + cmakeVersion;
        }
        if (multipleCompiler) {
            outputFolder = new File(outputFolder, toolset.c);
            zipName += "-" + toolset.c;
        }
        zipName += ".zip";
        File zip = new File(getOutputZipsFolder(workingDirectory), zipName).getAbsoluteFile();
        File buildFolder = new File(outputFolder, "cmake-generated-files");
        File redistFolder = new File(outputFolder, "redist").getAbsoluteFile();
        body("echo Building to %s\n", outputFolder);
        body("mkdir --parents %s/include", redistFolder);

        body(String.format(
                "%s \\\n" +
                "   -H%s \\\n" +
                "   -B%s \\\n" +
                "   -DCMAKEIFY_REDIST_INCLUDE_DIRECTORY=%s/include \\\n" +
                "   -DCMAKE_LIBRARY_OUTPUT_DIRECTORY=%s/lib \\\n" +
                "   -DCMAKE_SYSTEM_NAME=Linux \\\n" +
                "   -DCMAKE_C_COMPILER=%s \\\n" +
                "   -DCMAKE_CXX_COMPILER=%s",
                cmakeExe, workingDirectory, buildFolder,
            redistFolder, redistFolder, toolset.c, toolset.cxx));

        body(String.format("%s --build %s", cmakeExe, buildFolder));
        body("rc=$?; if [[ $rc != 0 ]]; then exit $rc; fi");
        redistFolderToZip.put(redistFolder.getAbsolutePath(), zip.getAbsolutePath());

        return this;
    }

    @Override
    ScriptBuilder buildRedistFiles() {
        for(String redistFolder : redistFolderToZip.keySet()) {
            String zip = redistFolderToZip.get(redistFolder);
            body("pushd %s", redistFolder);
            body("zip %s . -r", zip);
            body("popd");
            body("rc=$?; if [[ $rc != 0 ]]; then exit $rc; fi");
        }
        for(String redistFolder : redistFolderToZip.keySet()) {
            String zip = redistFolderToZip.get(redistFolder);
            body("echo - %s", new File(".").toURI().relativize(new File(zip).toURI()).getPath());
        }
        return this;
    }

    @Override
    public String toString() {
        return body.toString();
    }
}
