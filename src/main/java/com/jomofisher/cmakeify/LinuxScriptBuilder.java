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
    final private static String ABORT_LAST_FAILED = "rc=$?; if [[ $rc != 0 ]]; then exit $rc; fi";
    final private static String TOOLS_FOLDER = ".cmakeify/tools";
    final private static String DOWNLOADS_FOLDER = ".cmakeify/downloads";
    final private StringBuilder body = new StringBuilder();
    final private Map<String, String> zips = new HashMap<>();
    final private File workingFolder;
    final private File rootBuildFolder;
    final private File zipsFolder;
    final private File cdepFile;
    final private File androidFolder;
    final private String targetGroupId;
    final private String targetArtifactId;
    final private String targetVersion;
    final private String cmakeifyVersion;

    LinuxScriptBuilder(
            String cmakeifyVersion,
            File workingFolder,
            String targetGroupId,
            String targetArtifactId,
            String targetVersion) {
        this.cmakeifyVersion = cmakeifyVersion;
        this.workingFolder = workingFolder;
        this.rootBuildFolder = new File(workingFolder, "build");
        this.zipsFolder = new File(rootBuildFolder, "zips");
        this.cdepFile = new File(zipsFolder, "cdep-manifest.yml");
        this.androidFolder = new File(rootBuildFolder, "Android");
        this.targetGroupId = targetGroupId;
        this.targetArtifactId = targetArtifactId;
        this.targetVersion = targetVersion;
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
    ScriptBuilder createEmptyBuildFolder() {
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
            body("  echo CMAKEIFY ERROR: Missing %s. Please install.", compiler);
            body("  exit 100");
            body("fi");
        }
        return this;
    }

    @Override
    ScriptBuilder cmakeAndroid(String cmakeVersion,
                               RemoteArchive cmakeRemote,
                               String flavor,
                               String flavorFlags,
                               String ndkVersion,
                               RemoteArchive ndkRemote,
                               String includes[],
                               String lib,
                               String compiler,
                               String runtime,
                               String platform,
                               String abis[],
                               boolean multipleFlavors,
                               boolean multipleCMake,
                               boolean multipleNDK,
                               boolean multipleCompiler,
                               boolean multipleRuntime,
                               boolean multiplePlatforms) {
        body("echo Executing script for %s %s %s %s %s", flavor, ndkVersion, platform, compiler, runtime);
        String cmakeExe = String.format("%s/%s/bin/cmake", TOOLS_FOLDER,
                cmakeRemote.linux.unpackroot);
        File outputFolder = androidFolder;
        String zipName = targetArtifactId + "-android";
        if (multipleCMake) {
            outputFolder = new File(outputFolder, "cmake-" + cmakeVersion);
            zipName += "-cmake-" + cmakeVersion;
        }
        if (multipleNDK) {
            outputFolder = new File(outputFolder, ndkVersion);
            zipName += "-" + ndkVersion;
        }
        if (multipleCompiler) {
            outputFolder = new File(outputFolder, compiler);
            zipName += "-" + compiler;
        }
        if (multipleRuntime) {
            String fixedRuntime = runtime.replace('+', 'x');
            outputFolder = new File(outputFolder, fixedRuntime);
            zipName += "-" + fixedRuntime;
        }
        if (multiplePlatforms) {
            outputFolder = new File(outputFolder, "android-" + platform);
            zipName += "-platform-" + platform;
        }
        if (multipleFlavors) {
            outputFolder = new File(outputFolder, "flavor-" + flavor);
            zipName += "-" + flavor;
        }
        zipName += ".zip";
        File zip = new File(zipsFolder, zipName).getAbsoluteFile();

        File buildFolder = new File(outputFolder, "cmake-generated-files");
        String ndkFolder = String.format("%s/%s", TOOLS_FOLDER, ndkRemote.linux.unpackroot);
        File redistFolder = new File(outputFolder, "redist").getAbsoluteFile();
        File stagingFolder = new File(outputFolder, "staging").getAbsoluteFile();
        body("ABIS=");
        for (String abi : abis) {
            File abiBuildFolder = new File(buildFolder, abi);
            File archFolder = new File(String.format("%s/platforms/android-%s/arch-%s",
                    new File(ndkFolder).getAbsolutePath(), platform, Abi.getByName(abi).getArchitecture()));
            body("if [ -d '%s' ]; then", archFolder);
            body("  echo Creating make project in %s", abiBuildFolder);
            body("  if [[ \"$ABIS\" == \"\" ]]; then");
            body("    ABIS=%s", abi);
            body("  else");
            body("    ABIS=\"${ABIS}, %s\"", abi);
            body("  fi");

            String stagingAbiFolder = String.format("%s/lib/%s", stagingFolder, abi);

            String command = String.format(
                    "%s \\\n" +
                    "   -H%s \\\n" +
                    "   -B%s \\\n" +
                    "   -DCMAKE_ANDROID_NDK_TOOLCHAIN_VERSION=%s \\\n" +
                    "   -DCMAKE_ANDROID_NDK_TOOLCHAIN_DEBUG=1 \\\n" +
                    "   -DCMAKE_SYSTEM_NAME=Android \\\n" +
                    "   -DCMAKE_SYSTEM_VERSION=%s \\\n" +
                    "   -DCMAKEIFY_REDIST_INCLUDE_DIRECTORY=%s/include \\\n" +
                            "   -DCMAKE_LIBRARY_OUTPUT_DIRECTORY=%s \\\n" +
                            "   -DCMAKE_ARCHIVE_OUTPUT_DIRECTORY=%s  \\\n" +
                            "   -DCMAKE_ANDROID_STL_TYPE=%s_static \\\n" +
                    "   -DCMAKE_ANDROID_NDK=%s \\\n" +
                    "   -DCMAKE_ANDROID_ARCH_ABI=%s %s\n",
                    cmakeExe, workingFolder, abiBuildFolder, compiler, platform,
                    redistFolder, stagingAbiFolder, stagingAbiFolder, runtime,
                    new File(ndkFolder).getAbsolutePath(), abi, flavorFlags);
            body("  echo Executing %s", command);
            body("  " + command);
            body("  " + ABORT_LAST_FAILED);
            body(String.format("  %s --build %s", cmakeExe, abiBuildFolder));
            body("  " + ABORT_LAST_FAILED);
            String stagingLib = String.format("%s/%s", stagingAbiFolder, lib);
            String redistAbiFolder = String.format("%s/lib/%s", redistFolder, abi);
            if (lib != null && lib.length() > 0) {
                body("  if [ -f '%s' ]; then", stagingLib);
                body("    mkdir -p %s", redistAbiFolder);
                body("    cp %s %s/%s", stagingLib, redistAbiFolder, lib);
                body("    " + ABORT_LAST_FAILED);
                body("  else");
                body("    echo CMAKEIFY ERROR: CMake build did not produce %s", lib);
                body("    exit 100");
                body("  fi");
            }
            body("else");
            body("  echo Build skipped ABI %s because arch folder didnt exist: %s", abi, archFolder);
            body("fi");
            zips.put(zip.getAbsolutePath(), redistFolder.getPath());
        }
        body("if [ -d '%s' ]; then", stagingFolder);
        // Create a folder with something in it so there'e always something to zip
        body("  mkdir -p %s", redistFolder);
        body("  echo %s %s %s %s %s %s > %s/cmakeify.txt", cmakeVersion, flavor, ndkVersion, platform, compiler,
                runtime, redistFolder);
        if (includes != null) {
            for (String include : includes) {
                body("  cp -r %s/%s %s/include", workingFolder, include, redistFolder);
                body("  " + ABORT_LAST_FAILED);
            }
        }
        body("  if [ -f '%s' ]; then", zip);
        body("    echo CMAKEIFY ERROR: Android zip %s would be overwritten", zip);
        body("    exit 400");
        body("  fi");
        body("  pushd %s", redistFolder);
        body("  " + ABORT_LAST_FAILED);
        body("  zip %s . -r", zip);
        body("  " + ABORT_LAST_FAILED);
        body("  if [ -f '%s' ]; then", zip);
        body("    echo Zip %s was created", zip);
        body("  else");
        body("    echo CMAKEIFY ERROR: Zip %s was not created", zip);
        body("    exit 402");
        body("  fi");
        body("  popd");
        body("  " + ABORT_LAST_FAILED);
        body("  SHASUM256=$(shasum -a 256 %s | awk '{print $1}')", zip);
        body("  ARCHIVESIZE=$(stat --printf=\"%%s\" %s)", zip);
        body("  " + ABORT_LAST_FAILED);

        cdep("- lib: %s", lib);
        cdep("  archives:");
        cdep("  - file: %s", zip.getName());
        cdep("    sha256: $SHASUM256");
        cdep("    size: $ARCHIVESIZE");
        if (multipleFlavors) {
            cdep("  flavor: %s", flavor);
        }
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
        if (lib == null || lib.length() > 0) {
            body("else");
            body("  echo CMAKEIFY ERROR: Build did not produce an output in %s", stagingFolder);
            body("  exit 200");
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
        String zipName = targetArtifactId + "-linux";
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
        body(ABORT_LAST_FAILED);
        zips.put(zip.getAbsolutePath(), redistFolder.getPath());
        body("# Zip Linux redist if folder was created in %s", redistFolder);
        body("if [ -d '%s' ]; then", redistFolder);
        body("  if [ -f '%s' ]; then", zip);
        body("    echo CMAKEIFY ERROR: Linux zip %s would be overwritten", zip);
        body("    exit 500");
        body("  fi");
        body("  pushd %s", redistFolder);
        body("  " + ABORT_LAST_FAILED);
        body("  zip %s . -r", zip);
        body("  " + ABORT_LAST_FAILED);
        body("  if [ -f '%s' ]; then", zip);
        body("    echo Zip %s was created", zip);
        body("  else");
        body("    echo CMAKEIFY ERROR: Zip %s was not created", zip);
        body("    exit 402");
        body("  fi");
        body("  popd");
        body("  " + ABORT_LAST_FAILED);
        body("  SHASUM256=$(shasum -a 256 %s | awk '{print $1}')", zip);
        body("  " + ABORT_LAST_FAILED);
        body("fi");
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
    ScriptBuilder buildRedistFiles(File workingFolder, String[] includes, String example) {
        if (example != null && example.length() > 0) {
            cdep("example: |");
            String lines[] = example.split("\\r?\\n");
            for (String line : lines) {
                cdep("  %s", line);
            }
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
    ScriptBuilder uploadBadges() {
        // Record build information
        String badgeUrl = String.format("%s:%s:%s", targetGroupId, targetArtifactId, targetVersion);
        badgeUrl = badgeUrl.replace(":", "%3A");
        badgeUrl = badgeUrl.replace("-", "--");
        badgeUrl = String.format("https://img.shields.io/badge/cdep-%s-brightgreen.svg", badgeUrl);
        String badgeFolder = String.format("%s/%s",
            targetGroupId,
            targetArtifactId);
        body("if [ -n \"$TRAVIS_TAG\" ]; then");
        body("  if [ -n \"$CDEP_BADGES_API_KEY\" ]; then");
        body("    git clone https://github.com/cdep-io/cdep-io.github.io.git");
        body("    pushd cdep-io.github.io");
        body("    mkdir -p %s/latest", badgeFolder);
        body("    echo curl %s > %s/latest/latest.svg ", badgeUrl, badgeFolder);
        body("    curl %s > %s/latest/latest.svg ", badgeUrl, badgeFolder);
        body("    " + ABORT_LAST_FAILED);
        body("    git add %s/latest/latest.svg", badgeFolder);
        body("    git -c user.name='cmakeify' -c user.email='cmakeify' commit -m init");
        body("    git push -f -q https://cdep-io:$CDEP_BADGES_API_KEY@github.com/cdep-io/cdep-io.github.io &2>/dev/null");
        body("    popd");
        body("  else");
        body("    echo Add CDEP_BADGES_API_KEY to Travis settings to get badges!");
        body("  fi");
        body("fi");
        return this;
    }

    @Override
    public String toString() {
        return body.toString();
    }
}
