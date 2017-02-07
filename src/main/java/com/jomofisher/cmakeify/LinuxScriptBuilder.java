package com.jomofisher.cmakeify;

import com.jomofisher.cmakeify.model.OS;
import com.jomofisher.cmakeify.model.RemoteArchive;
import com.jomofisher.cmakeify.model.Toolset;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class LinuxScriptBuilder  extends ScriptBuilder {
    final private static String TOOLS_FOLDER = ".cmakeify/tools";
    final private static String DOWNLOADS_FOLDER = ".cmakeify/downloads";
    final private StringBuilder body = new StringBuilder();
    final private Map<String, String> zips = new HashMap<>();
    final private File workingFolder;
    final private File rootBuildFolder;
    final private File zipsFolder;
    final private File cdepFile;
    final private File androidFolder;
    final private String cmakeFlags;

    LinuxScriptBuilder(File workingFolder, String cmakeFlags) {
        this.workingFolder = workingFolder;
        this.rootBuildFolder = new File(workingFolder, "build");
        this.zipsFolder = new File(rootBuildFolder, "zips");
        this.cdepFile = new File(zipsFolder, "cdep-manifest.yml");
        this.androidFolder = new File(rootBuildFolder, "Android");
        this.cmakeFlags = cmakeFlags;
    }

    private LinuxScriptBuilder body(String format, Object... args) {
        body.append(String.format(format + "\n", args));
        return this;
    }

    private LinuxScriptBuilder cdep(String format, Object... args) {
        String embed = String.format(format, args);
        body.append(String.format("printf \"%%s\\r\\n\" \"%s\" >> %s \n", embed, cdepFile));
        return this;
    }

    @Override
    ScriptBuilder createEmptyBuildFolder(String targetGroupId, String targetArtifactId,
        String targetVersion) {
        body("rm -rf %s", rootBuildFolder);
        body("mkdir --parents %s", zipsFolder);
        body("mkdir --parents %s/", TOOLS_FOLDER);
        body("mkdir --parents %s/", DOWNLOADS_FOLDER);
        cdep("# Generated by CMakeify");
        cdep("coordinate:");
        cdep("  groupId: %s", targetGroupId);
        cdep("  artifactId: %s", targetArtifactId);
        cdep("  version: %s", targetVersion);
        return this;
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
    ScriptBuilder cmakeAndroid(String cmakeVersion,
                               RemoteArchive cmakeRemote,
                               String ndkVersion,
                               RemoteArchive ndkRemote,
                               String compiler,
                               String runtime,
                               String platform,
                               String abis[],
                               boolean multipleCMake,
                               boolean multipleNDK,
                               boolean multipleCompiler,
                               boolean multipleRuntime,
                               boolean multiplePlatforms) {
        String cmakeExe = String.format("%s/%s/bin/cmake %s", TOOLS_FOLDER,
                cmakeRemote.linux.unpackroot, cmakeFlags);
        File outputFolder = androidFolder;
        String zipName = workingFolder.getAbsoluteFile().getParentFile().getName() + "-android";
        if (multipleCMake) {
            outputFolder = new File(outputFolder, "cmake-" + cmakeVersion);
            zipName += "-cmake-" + cmakeVersion;
        }
        if (multipleNDK) {
            outputFolder = new File(outputFolder, ndkVersion);
            zipName += "-" + ndkVersion;
        }
        if (multipleCompiler) {
            outputFolder = new File(outputFolder, "-" + compiler);
            zipName += "-" + compiler;
        }
        if (multipleRuntime) {
            String fixedRuntime = runtime.replace('+', 'x');
            outputFolder = new File(outputFolder, "-" + fixedRuntime);
            zipName += "-" + fixedRuntime;
        }
        if (multiplePlatforms) {
            outputFolder = new File(outputFolder, "android-" + platform);
            zipName += "-platform-" + platform;
        }
        zipName += ".zip";
        File zip = new File(zipsFolder, zipName).getAbsoluteFile();

        File buildFolder = new File(outputFolder, "cmake-generated-files");
        String ndkFolder = String.format("%s/%s", TOOLS_FOLDER, ndkRemote.linux.unpackroot);
        File redistFolder = new File(outputFolder, "redist").getAbsoluteFile();
        body("ABIS=");
        for (String abi : abis) {
            File archFolder = new File(String.format("%s/platforms/android-%s/arch-%s",
                    new File(ndkFolder).getAbsolutePath(), platform, Abi.getByName(abi).getArchitecture()));
            body("if [ -d '%s' ]; then", archFolder);
            body("  echo Building to %s", outputFolder);
            body("  if [[ \"$ABIS\" == \"\" ]]; then");
            body("    ABIS=%s", abi);
            body("  else");
            body("    ABIS=\"${ABIS}, %s\"", abi);
            body("  fi");

            body(String.format(
                    "  %s \\\n" +
                    "   -H%s \\\n" +
                    "   -B%s \\\n" +
                    "   -DCMAKE_ANDROID_NDK_TOOLCHAIN_VERSION=%s \\\n" +
                    "   -DCMAKE_ANDROID_NDK_TOOLCHAIN_DEBUG=1 \\\n" +
                    "   -DCMAKE_SYSTEM_NAME=Android \\\n" +
                    "   -DCMAKE_SYSTEM_VERSION=%s \\\n" +
                    "   -DCMAKEIFY_REDIST_INCLUDE_DIRECTORY=%s/include \\\n" +
                    "   -DCMAKE_LIBRARY_OUTPUT_DIRECTORY=%s/lib/%s \\\n" +
                    "   -DCMAKE_ARCHIVE_OUTPUT_DIRECTORY=%s/lib/%s \\\n" +
                    "   -DCMAKE_ANDROID_STL_TYPE=%s \\\n" +
                    "   -DCMAKE_ANDROID_NDK=%s \\\n" +
                    "   -DCMAKE_ANDROID_ARCH_ABI=%s \n",
                    cmakeExe, workingFolder, buildFolder, compiler, platform,
                    redistFolder, redistFolder, abi, redistFolder, abi, runtime,
                new File(ndkFolder).getAbsolutePath(), abi));
            body(String.format("  %s --build %s", cmakeExe, buildFolder));
            body("  rc=$?; if [[ $rc != 0 ]]; then exit $rc; fi");
            zips.put(zip.getAbsolutePath(), redistFolder.getPath());
            body("fi");
        }
        body("if [ -d '%s' ]; then", redistFolder);
        cdep("- file: %s", zip.getName());
        cdep("  runtime: %s", runtime);
        cdep("  platform: %s", platform);
        cdep("  ndk: %s", ndkVersion);
        cdep("  abis: [ ${ABIS} ]");
        if (multipleCompiler) {
            cdep("  compiler: %s", compiler);
        }
        if (multipleCMake) {
            cdep("  builder: cmake-%s", cmakeVersion);
        }
        body("fi");
        return this;
    }

    @Override
    ScriptBuilder cmakeLinux(
            String cmakeVersion,
            RemoteArchive cmakeRemote,
            Toolset toolset,
            boolean multipleCMake,
            boolean multipleCompiler) {
        String cmakeExe = String.format("%s/%s/bin/cmake", TOOLS_FOLDER,
            cmakeRemote.linux.unpackroot);
        File outputFolder = new File(rootBuildFolder, "Linux");
        String zipName = workingFolder.getAbsoluteFile().getParentFile().getName() + "-linux";
        if (multipleCMake) {
            outputFolder = new File(outputFolder,  "cmake-" + cmakeVersion);
            zipName += "-cmake-" + cmakeVersion;
        }
        if (multipleCompiler) {
            outputFolder = new File(outputFolder, toolset.c);
            zipName += "-" + toolset.c;
        }
        zipName += ".zip";
        File zip = new File(zipsFolder, zipName).getAbsoluteFile();
        File buildFolder = new File(outputFolder, "cmake-generated-files");
        File redistFolder = new File(outputFolder, "redist").getAbsoluteFile();
        body("echo Building to %s", outputFolder);
        body("mkdir --parents %s/include", redistFolder);

        body(String.format(
                "%s \\\n" +
                "   -H%s \\\n" +
                "   -B%s \\\n" +
                "   -DCMAKEIFY_REDIST_INCLUDE_DIRECTORY=%s/include \\\n" +
                "   -DCMAKE_LIBRARY_OUTPUT_DIRECTORY=%s/lib \\\n" +
                "   -DCMAKE_ARCHIVE_OUTPUT_DIRECTORY=%s/lib \\\n" +
                "   -DCMAKE_SYSTEM_NAME=Linux \\\n" +
                "   -DCMAKE_C_COMPILER=%s \\\n" +
                "   -DCMAKE_CXX_COMPILER=%s",
                cmakeExe, workingFolder, buildFolder,
                redistFolder, redistFolder, redistFolder, toolset.c, toolset.cxx));

        body(String.format("%s --build %s", cmakeExe, buildFolder));
        body("rc=$?; if [[ $rc != 0 ]]; then exit $rc; fi");
        zips.put(zip.getAbsolutePath(), redistFolder.getPath());

        return this;
    }

    @Override
    ScriptBuilder startBuilding(OS target) {
        switch(target) {
            case android:
                cdep("android:");
                return this;
            case linux:
                cdep("linux:");
                return this;
            case windows:
                cdep("windows:");
                return this;
        }
        throw new RuntimeException(target.toString());
    }

    @Override
    ScriptBuilder buildRedistFiles(File workingFolder) {
        for(String zip : zips.keySet()) {
            String redistFolder = zips.get(zip);
            body("if [ -d '%s' ]; then", redistFolder);
            body("  pushd %s", redistFolder);
            body("  rc=$?; if [[ $rc != 0 ]]; then exit $rc; fi");
            body("  zip %s . -r", zip);
            body("  rc=$?; if [[ $rc != 0 ]]; then exit $rc; fi");
            body("  popd");
            body("  rc=$?; if [[ $rc != 0 ]]; then exit $rc; fi");
            body("fi");
        }
        body("cat %s", cdepFile);
        body("echo - %s", cdepFile);
        for(String zip : zips.keySet()) {
            String relativeZip = new File(".").toURI().relativize(new File(zip).toURI()).getPath();
            body("if [ -f '%s' ]; then", relativeZip);
            body("  echo - %s", relativeZip);
            body("fi");
        }
        return this;
    }

    @Override
    public String toString() {
        return body.toString();
    }
}
