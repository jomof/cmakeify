package com.jomofisher.cmakeify;

import com.jomofisher.cmakeify.CMakeify.OSType;
import com.jomofisher.cmakeify.model.*;

import java.io.*;
import java.util.*;

public class BashScriptBuilder extends ScriptBuilder {

    final private static String ABORT_LAST_FAILED = "rc=$?; if [[ $rc != 0 ]]; then exit -$rc; fi";
    final private static String TOOLS_FOLDER = ".cmakeify/tools";
    final private static String DOWNLOADS_FOLDER = ".cmakeify/downloads";
    final private StringBuilder body = new StringBuilder();
    final private Map<String, String> zips = new HashMap<>();
    final private OSType hostOS;
    final private File workingFolder;
    final private File rootBuildFolder;
    final private File rootInstallFolder;
    final private File zipsFolder;
    final private File cdepFile;
    final private File androidBuildFolder;
    final private String targetGroupId;
    final private String targetArtifactId;
    final private String targetArtifactIdFolderName;
    final private String repo;
    final private String targetVersion;
    final private String fileSuffix;
    final private String filePrefix;
    final private Set<File> outputLocations = new HashSet<>();
    final private PrintStream out;
    final private OS specificTargetOS;
    final private boolean install;

    BashScriptBuilder(PrintStream out,
                      OSType hostOS,
                      File workingFolder,
                      String targetGroupId,
                      String targetArtifactId,
                      String targetVersion,
                      OS specificTargetOS,
                      boolean install) {
        this.out = out;
        this.hostOS = hostOS;
        this.workingFolder = workingFolder;
        this.rootBuildFolder = new File(workingFolder, "build");
        this.rootInstallFolder = new File(workingFolder, "install").getAbsoluteFile();
        this.zipsFolder = new File(rootBuildFolder, "zips");
        this.androidBuildFolder = new File(rootBuildFolder, "Android");
        this.targetGroupId = targetGroupId;
        this.targetArtifactId = targetArtifactId;
        this.targetArtifactIdFolderName = targetArtifactId.replace("/", "-");
        this.targetVersion = targetVersion;
        this.specificTargetOS = specificTargetOS;
        if (targetArtifactId.contains("/")) {
            this.repo = targetArtifactId.split("/")[0];
            this.fileSuffix = "-" + targetArtifactId.split("/")[1];
            this.filePrefix = targetArtifactIdFolderName + "-";
        } else {
            this.repo = targetArtifactId;
            this.fileSuffix = "";
            this.filePrefix = "";
        }
        this.install = install;
        if (generateCDep()) {
            if (specificTargetOS == null) {
                this.cdepFile = new File(zipsFolder, "cdep-manifest" + fileSuffix + ".yml");
            } else {
                this.cdepFile = new File(zipsFolder, String.format("cdep-manifest-%s.yml", specificTargetOS));
            }
        } else {
            this.cdepFile = null;
        }

    }

    private BashScriptBuilder body(String format, Object... args) {
        String write = String.format(format + "\n", args);
        if (write.contains(">")) {
            throw new RuntimeException(write);
        }
        if (write.contains("<")) {
            throw new RuntimeException(write);
        }
        if (write.contains("&")) {
            throw new RuntimeException(write);
        }
        body.append(write);
        return this;
    }

    private BashScriptBuilder bodyWithRedirect(String format, Object... args) {
        String write = String.format(format + "\n", args);
        if (!write.contains(">")) {
            throw new RuntimeException(write);
        }
        if (write.contains("<")) {
            throw new RuntimeException(write);
        }
        body.append(write);
        return this;
    }

    private boolean generateCDep() {
        return (this.targetGroupId != null && this.targetGroupId.length() > 0);
    }

    private BashScriptBuilder cdep(String format, Object... args) {
        if (!generateCDep()) {
            return this;
        }

        String embed = String.format(format, args);
        body.append(String.format("printf \"%%s\\r\\n\" \"%s\" >> %s \n", embed, cdepFile));
        return this;
    }

    private void recordOutputLocation(File folder) {
        out.printf("Writing to %s\n", folder);
        if (this.outputLocations.contains(folder)) {
            throw new RuntimeException(String.format("Output location %s written twice", folder));
        }

        try {
            File canonical = folder.getCanonicalFile();
            if (this.outputLocations.contains(canonical)) {
                throw new RuntimeException(String.format("Output location %s written twice", folder));
            }
            this.outputLocations.add(folder);
            this.outputLocations.add(canonical);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    ScriptBuilder createEmptyBuildFolder(HardNameDependency dependencies[]) {
        if (generateCDep()) {
            body("cdep=$(pwd)/cdep");
            body("echo Using cdep at ${cdep}");
        }
        body("rm -rf %s", rootBuildFolder);
        body("mkdir -p %s", zipsFolder);
        body("mkdir -p %s/", TOOLS_FOLDER);
        body("mkdir -p %s/", DOWNLOADS_FOLDER);
        cdep("# Generated by CMakeify");
        cdep("coordinate:");
        cdep("  groupId: %s", targetGroupId);
        cdep("  artifactId: %s", targetArtifactId);
        cdep("  version: %s", targetVersion);
        if (dependencies != null && dependencies.length > 0) {
            cdep("dependencies:");
            for (HardNameDependency dependency : dependencies) {
                cdep("  - compile: %s", dependency.compile);
                cdep("    sha256: %s", dependency.sha256);
            }
        }
        return this;
    }

    private ArchiveUrl getHostArchive(RemoteArchive remote) {
        switch (hostOS) {
            case Linux:
                return remote.linux;
            case MacOS:
                return remote.darwin;
        }
        throw new RuntimeException(hostOS.toString());
    }

    @Override
    ScriptBuilder download(RemoteArchive remote) {
        ArchiveInfo archive = new ArchiveInfo(getHostArchive(remote));
        return bodyWithRedirect(archive.downloadToFolder(DOWNLOADS_FOLDER)).bodyWithRedirect(archive.uncompressToFolder(
                DOWNLOADS_FOLDER,
                TOOLS_FOLDER));
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
            body("  exit -110");
            body("fi");
        }
        return this;
    }

    @Override
    ScriptBuilder cmakeAndroid(String cmakeVersion,
                               RemoteArchive cmakeRemote,
                               List<String> targets,
                               String cmakeFlags,
                               String flavor,
                               String flavorFlags,
                               String ndkVersion,
                               RemoteArchive ndkRemote,
                               String includes[],
                               String compiler,
                               String runtime,
                               String platform,
                               String abi,
                               boolean multipleFlavors,
                               boolean multipleCMake,
                               boolean multipleNDK,
                               boolean multipleCompiler,
                               boolean multipleRuntime,
                               boolean multiplePlatforms,
                               boolean multipleAbi) {
        body("echo Executing script for %s %s %s %s %s %s %s", flavor, ndkVersion, platform, compiler, runtime, targets, abi);
        List<String> libs = new ArrayList<>();
        for (String target : targets) {
            libs.add(String.format("lib%s.a", target));
        }
        if (cmakeFlags == null) {
            cmakeFlags = "";
        }
        String cmakeExe = String.format("%s/%s/bin/cmake", TOOLS_FOLDER, getHostArchive(cmakeRemote).unpackroot);
        File outputFolder = androidBuildFolder;
        File installFolder = new File(rootInstallFolder, "Android");
        String zipName = targetArtifactIdFolderName + "-android";
        if (multipleCMake) {
            outputFolder = new File(outputFolder, "cmake-" + cmakeVersion);
            installFolder = new File(installFolder, "cmake-" + cmakeVersion);
            zipName += "-cmake-" + cmakeVersion;
        }
        if (multipleNDK) {
            outputFolder = new File(outputFolder, ndkVersion);
            installFolder = new File(installFolder, ndkVersion);
            zipName += "-" + ndkVersion;
        }
        if (multipleCompiler) {
            outputFolder = new File(outputFolder, compiler);
            installFolder = new File(installFolder, compiler);
            zipName += "-" + compiler;
        }
        if (multipleRuntime) {
            String fixedRuntime = runtime.replace('+', 'x');
            outputFolder = new File(outputFolder, fixedRuntime);
            installFolder = new File(installFolder, fixedRuntime);
            zipName += "-" + fixedRuntime;
        }
        if (multiplePlatforms) {
            outputFolder = new File(outputFolder, "android-" + platform);
            installFolder = new File(installFolder, "android-" + platform);
            zipName += "-platform-" + platform;
        }
        if (multipleFlavors) {
            outputFolder = new File(outputFolder, "flavor-" + flavor);
            installFolder = new File(installFolder, "flavor-" + flavor);
            zipName += "-" + flavor;
        }
        if (multipleAbi) {
            outputFolder = new File(outputFolder, "abi-" + abi);
            zipName += "-" + abi;
        }
        zipName += ".zip";
        File zip = new File(zipsFolder, zipName).getAbsoluteFile();
        File headers = new File(zipsFolder, filePrefix + "headers.zip").getAbsoluteFile();
        recordOutputLocation(zip);

        File buildFolder = new File(outputFolder, "cmake-generated-files");
        String ndkFolder = String.format("%s/%s", TOOLS_FOLDER, getHostArchive(ndkRemote).unpackroot);
        File redistFolder = new File(outputFolder, "redist").getAbsoluteFile();
        File headerFolder = new File(outputFolder, "header").getAbsoluteFile();
        File stagingFolder = new File(outputFolder, "staging").getAbsoluteFile();

        File abiInstallFolder = new File(installFolder, abi).getAbsoluteFile();
        File abiBuildFolder = new File(buildFolder, abi);
        File archFolder = new File(String.format("%s/platforms/android-%s/arch-%s",
                new File(ndkFolder).getAbsolutePath(),
                platform,
                Abi.getByName(abi).getArchitecture()));
        body("if [ -d '%s' ]; then", archFolder);
        body("  echo Creating make project in %s", abiBuildFolder);

        File stagingAbiFolder = new File(String.format("%s/lib/%s", stagingFolder, abi));
        recordOutputLocation(stagingAbiFolder);
        String command = String.format("%s --install \\\n" +
                        "   -H%s \\\n" +
                        "   -B%s \\\n" +
                        "   -DCMAKE_ANDROID_NDK_TOOLCHAIN_VERSION=%s \\\n" +
                        "   -DCMAKE_ANDROID_NDK_TOOLCHAIN_DEBUG=1 \\\n" +
                        "   -DCMAKE_SYSTEM_NAME=Android \\\n" +
                        "   -DCMAKE_SYSTEM_VERSION=%s \\\n" +
                        "   -DCMAKEIFY_REDIST_INCLUDE_DIRECTORY=%s/include \\\n" +
                        "   -DCMAKE_LIBRARY_OUTPUT_DIRECTORY=%s \\\n" +
                        "   -DCMAKE_ARCHIVE_OUTPUT_DIRECTORY=%s \\\n" +
                        "   -DCMAKE_INSTALL_PREFIX:PATH=%s \\\n" +
                        "   -DCMAKE_ANDROID_STL_TYPE=%s_static \\\n" +
                        "   -DCMAKE_ANDROID_NDK=%s \\\n" +
                        "   -DCMAKE_ANDROID_ARCH_ABI=%s %s %s\n",
                cmakeExe,
                workingFolder,
                abiBuildFolder,
                compiler,
                platform,
                headerFolder,
                stagingAbiFolder,
                stagingAbiFolder,
                abiInstallFolder,
                runtime,
                new File(ndkFolder).getAbsolutePath(),
                abi,
                flavorFlags,
                cmakeFlags);
        body("  echo Executing %s", command);
        body("  " + command);
        body("  " + ABORT_LAST_FAILED);
        if (targets.size() > 0) {
            for (String target : targets) {
                body(String.format("  %s --build %s --target %s -- -j8", cmakeExe, abiBuildFolder, target));
            }
        } else {
            body(String.format("  %s --build %s -- -j8", cmakeExe, abiBuildFolder));
        }
        if (install) {
            body(String.format("  echo %s --build %s --target install -- -j8", cmakeExe, abiBuildFolder));
            body(String.format("  %s --build %s --target install -- -j8", cmakeExe, abiBuildFolder));
        }
        body("  " + ABORT_LAST_FAILED);

        File redistAbiFolder = new File(String.format("%s/lib/%s", redistFolder, abi));
        recordOutputLocation(redistAbiFolder);

        for (String lib : libs) {
            if (lib != null && lib.length() > 0) {
                String stagingLib = String.format("%s/%s", stagingAbiFolder, lib);
                body("  if [ -f '%s' ]; then", stagingLib);
                body("    mkdir -p %s", redistAbiFolder);
                body("    cp %s %s/%s", stagingLib, redistAbiFolder, lib);
                body("    " + ABORT_LAST_FAILED);
                body("  else");
                body("    echo CMAKEIFY ERROR: CMake build did not produce %s", stagingLib);
                body("    exit -100");
                body("  fi");
            } else {
                body("  echo cmakeify.yml did not specify lib or target. No output library expected.");
            }
        }
        body("else");
        body("  echo Build skipped ABI %s because arch folder didnt exist: %s", abi, archFolder);
        body("fi");
        zips.put(zip.getAbsolutePath(), redistFolder.getPath());

        body("if [ -d '%s' ]; then", stagingFolder);
        // Create a folder with something in it so there'e always something to zip
        body("  mkdir -p %s", redistFolder);
        bodyWithRedirect("  echo Android %s %s %s %s %s %s > %s/cmakeify.txt",
                cmakeVersion,
                flavor,
                ndkVersion,
                platform,
                compiler,
                runtime,
                redistFolder);
        writeExtraIncludesToBody(includes, headerFolder);
        writeCreateZipFromRedistFolderToBody(zip, redistFolder);
        writeCreateHeaderZip(headers, headerFolder);
        writeZipFileStatisticsToBody(zip);
        StringBuilder libStrings = new StringBuilder();
        for (int i = 0; i < libs.size(); ++i) {
            if (i != 0) {
                libStrings.append(",");
            }
            libStrings.append(libs.get(i));
        }
        cdep("  - libs: [%s]", libStrings.toString());
        cdep("    file: %s", zip.getName());
        cdep("    sha256: $SHASUM256");
        cdep("    size: $ARCHIVESIZE");
        if (multipleFlavors) {
            cdep("    flavor: %s", flavor);
        }
        cdep("    runtime: %s", runtime);
        cdep("    platform: %s", platform);
        cdep("    ndk: %s", ndkVersion);
        cdep("    abi: %s", abi);
        if (multipleCompiler) {
            cdep("    compiler: %s", compiler);
        }
        if (multipleCMake) {
            cdep("    builder: cmake-%s", cmakeVersion);
        }
        body("fi");

        return this;
    }

    private void writeCreateHeaderZip(File headers, File headerFolder) {
        body("  if [ -d '%s' ]; then", headerFolder);
        writeCreateZipFromRedistFolderToBody(headers, headerFolder);
        body("  else");
        if (install) {
            body("    echo CMAKEIFY WARNING: Header folder %s was not found", headerFolder);
        } else {
            body("    echo CMAKEIFY ERROR: Header folder %s was not found", headerFolder);
            body("    exit -699");
        }
        body("  fi");
    }

    private void writeZipFileStatisticsToBody(File zip) {
        body("  SHASUM256=$(shasum -a 256 %s | awk '{print $1}')", zip);
        body("  " + ABORT_LAST_FAILED);
        body("  ARCHIVESIZE=$(ls -l %s | awk '{print $5}')", zip);
        body("  " + ABORT_LAST_FAILED);
    }

    @Override
    ScriptBuilder cmakeLinux(String cmakeVersion,
                             RemoteArchive cmakeRemote,
                             String target,
                             String cmakeFlags,
                             Toolset toolset,
                             String includes[],
                             String lib,
                             boolean multipleCMake,
                             boolean multipleCompiler) {
        if (target != null && target.length() > 0 && lib != null && lib.length() > 0) {
            throw new RuntimeException("cmakify.yml has both lib and target, only one is allowed");
        }
        if (target != null && target.length() > 0 && (lib == null || lib.length() == 0)) {
            lib = String.format("lib%s.a", target);
        }
        if (cmakeFlags == null) {
            cmakeFlags = "";
        }
        String cmakeExe = String.format("%s/%s/bin/cmake", TOOLS_FOLDER, getHostArchive(cmakeRemote).unpackroot);
        File outputFolder = new File(rootBuildFolder, "Linux");
        File installFolder = new File(rootInstallFolder, "Linux");
        String zipName = targetArtifactIdFolderName + "-linux";
        if (multipleCMake) {
            outputFolder = new File(outputFolder, "cmake-" + cmakeVersion);
            installFolder = new File(installFolder, "cmake-" + cmakeVersion);
            zipName += "-cmake-" + cmakeVersion;
        }
        if (multipleCompiler) {
            outputFolder = new File(outputFolder, toolset.c);
            installFolder = new File(installFolder, toolset.c);
            zipName += "-" + toolset.c;
        }
        zipName += ".zip";
        File zip = new File(zipsFolder, zipName).getAbsoluteFile();
        File headers = new File(zipsFolder, filePrefix + "headers.zip").getAbsoluteFile();
        File buildFolder = new File(outputFolder, "cmake-generated-files");
        File headerFolder = new File(outputFolder, "header").getAbsoluteFile();
        File redistFolder = new File(outputFolder, "redist").getAbsoluteFile();

        body("echo Building to %s", outputFolder);
        body("mkdir -p %s/include", redistFolder);
        recordOutputLocation(zip);
        recordOutputLocation(outputFolder);
        recordOutputLocation(redistFolder);

        body(String.format("%s --install \\\n" +
                        "  -H%s \\\n" +
                        "  -B%s \\\n" +
                        "  -DCMAKEIFY_REDIST_INCLUDE_DIRECTORY=%s/include \\\n" +
                        "  -DCMAKE_LIBRARY_OUTPUT_DIRECTORY=%s/lib \\\n" +
                        "  -DCMAKE_ARCHIVE_OUTPUT_DIRECTORY=%s/lib \\\n" +
                        "  -DCMAKE_SYSTEM_NAME=Linux \\\n" +
                        "  -DCMAKE_SYSTEM_NAME=Linux \\\n" +
                        "  -DCMAKE_C_COMPILER=%s \\\n" +
                        "  -DCMAKE_INSTALL_PREFIX:PATH=%s \\\n" +
                        "  -DCMAKE_CXX_COMPILER=%s %s",
                cmakeExe,
                workingFolder,
                buildFolder,
                headerFolder,
                redistFolder,
                redistFolder,
                toolset.c,
                installFolder,
                toolset.cxx,
                cmakeFlags));

        if (target != null && target.length() > 0) {
            body(String.format("%s --build %s --target %s -- -j8", cmakeExe, buildFolder, target));
        } else {
            body(String.format("%s --build %s -- -j8", cmakeExe, buildFolder));
        }
        body(ABORT_LAST_FAILED);
        if (install) {
            body(String.format("%s --build %s --target install", cmakeExe, buildFolder));
            body("  " + ABORT_LAST_FAILED);
        }
        zips.put(zip.getAbsolutePath(), redistFolder.getPath());
        body("# Zip Linux redist if folder was created in %s", redistFolder);
        body("if [ -d '%s' ]; then", redistFolder);
        body("  if [ -f '%s' ]; then", zip);
        body("    echo CMAKEIFY ERROR: Linux zip %s would be overwritten", zip);
        body("    exit -500");
        body("  fi");
        writeExtraIncludesToBody(includes, headerFolder);
        writeCreateZipFromRedistFolderToBody(zip, redistFolder);
        writeCreateHeaderZip(headers, headerFolder);
        writeZipFileStatisticsToBody(zip);
        body("  " + ABORT_LAST_FAILED);
        cdep("  - lib: %s", lib);
        cdep("    file: %s", zip.getName());
        cdep("    sha256: $SHASUM256");
        cdep("    size: $ARCHIVESIZE");
        body("else");
        body("  echo CMAKEIFY ERROR: Did not create %s", redistFolder);
        body("  exit -520");
        body("fi");
        return this;
    }

    @Override
    ScriptBuilder cmakeiOS(String cmakeVersion,
                           RemoteArchive cmakeRemote,
                           String target,
                           String cmakeFlags,
                           String flavor,
                           String flavorFlags,
                           String includes[],
                           String lib,
                           iOSPlatform platform,
                           iOSArchitecture architecture,
                           String sdk,
                           boolean multipleFlavor,
                           boolean multipleCMake,
                           boolean multiplePlatform,
                           boolean multipleArchitecture,
                           boolean multipleSdk) {
        if (target != null && target.length() > 0 && lib != null && lib.length() > 0) {
            throw new RuntimeException("cmakify.yml has both lib and target, only one is allowed");
        }
        if (target != null && target.length() > 0 && (lib == null || lib.length() == 0)) {
            lib = String.format("lib%s.a", target);
        }
        if (cmakeFlags == null) {
            cmakeFlags = "";
        }
        if (!isSupportediOSPlatformArchitecture(platform, architecture)) {
            out.printf("Skipping iOS %s %s because it isn't supported by XCode\n", platform, architecture);
            return this;
        }

        String cmakeExe = String.format("%s/%s/bin/cmake", TOOLS_FOLDER, getHostArchive(cmakeRemote).unpackroot);
        File outputFolder = new File(rootBuildFolder, "iOS");
        File installFolder = new File(rootInstallFolder, "iOS");
        String zipName = targetArtifactIdFolderName + "-ios";
        if (multipleCMake) {
            outputFolder = new File(outputFolder, "cmake-" + cmakeVersion);
            installFolder = new File(installFolder, "cmake-" + cmakeVersion);
            zipName += "-cmake-" + cmakeVersion;
        }
        if (multipleFlavor) {
            outputFolder = new File(outputFolder, "flavor-" + flavor);
            installFolder = new File(installFolder, "flavor-" + flavor);
            zipName += "-" + flavor;
        }
        if (multiplePlatform) {
            outputFolder = new File(outputFolder, "platform-" + platform.toString());
            installFolder = new File(installFolder, "platform-" + platform.toString());
            zipName += "-platform-" + platform.toString();
        }
        if (multipleArchitecture) {
            outputFolder = new File(outputFolder, "architecture-" + architecture.toString());
            installFolder = new File(installFolder, "architecture-" + architecture.toString());
            zipName += "-architecture-" + architecture.toString();
        }
        if (multipleSdk) {
            outputFolder = new File(outputFolder, "sdk-" + sdk);
            installFolder = new File(installFolder, "sdk-" + sdk);
            zipName += "-sdk-" + sdk;
        }

        zipName += ".zip";
        File zip = new File(zipsFolder, zipName).getAbsoluteFile();
        File headers = new File(zipsFolder, filePrefix + "headers.zip").getAbsoluteFile();
        File buildFolder = new File(outputFolder, "cmake-generated-files");
        File headerFolder = new File(outputFolder, "header").getAbsoluteFile();
        File redistFolder = new File(outputFolder, "redist").getAbsoluteFile();
        File stagingFolder = new File(outputFolder, "staging").getAbsoluteFile();
        if (hostOS != OSType.MacOS) {
            body("echo No XCode available. NOT building to %s", outputFolder);
        } else {
            body("CDEP_IOS_CLANG=$(xcrun -sdk iphoneos -find clang)");
            body("CDEP_IOS_AR=$(xcrun -sdk iphoneos -find ar)");
            body("CDEP_XCODE_DEVELOPER_DIR=$(xcode-select -print-path)");
            body("CDEP_IOS_DEVELOPER_ROOT=${CDEP_XCODE_DEVELOPER_DIR}/Platforms/%s.platform/Developer", platform);
            body("CDEP_IOS_SDK_ROOT=${CDEP_IOS_DEVELOPER_ROOT}/SDKs/%s%s.sdk", platform, sdk);
            body("if [ ! -d \"${CDEP_IOS_SDK_ROOT}\" ]; then");
            body("  echo Not building for non-existent SDK root ${CDEP_IOS_SDK_ROOT}. Listing available:");
            body("  ls ${CDEP_IOS_DEVELOPER_ROOT}/SDKs");
            body("else");
            body("  echo Building to %s", outputFolder);
            body("  mkdir -p %s/include", redistFolder);
        }
        recordOutputLocation(zip);
        recordOutputLocation(outputFolder);
        recordOutputLocation(redistFolder);
        recordOutputLocation(stagingFolder);

        String command = String.format("%s \\\n" +
                        "    -H%s \\\n" +
                        "    -B%s \\\n" +
                        "    -DCMAKE_C_COMPILER=${CDEP_IOS_CLANG}\\\n" +
                        "    -DCMAKE_CXX_COMPILER=${CDEP_IOS_CLANG} \\\n" +
                        "    -DCMAKE_C_COMPILER_WORKS=1 \\\n" +
                        "    -DCMAKE_CXX_COMPILER_WORKS=1 \\\n" +
                        "    -DCMAKE_AR=${CDEP_IOS_AR}\\\n" +
                        "    -DCMAKE_OSX_SYSROOT=${CDEP_IOS_SDK_ROOT} \\\n" +
                        "    -DCMAKE_OSX_ARCHITECTURES=%s \\\n" +
                        "    -DCMAKEIFY_REDIST_INCLUDE_DIRECTORY=%s/include \\\n" +
                        "  -DCMAKE_INSTALL_PREFIX:PATH=%s \\\n" +
                        "    -DCMAKE_LIBRARY_OUTPUT_DIRECTORY=%s/lib \\\n" +
                        "    -DCMAKE_ARCHIVE_OUTPUT_DIRECTORY=%s/lib %s %s \\\n",
                cmakeExe,
                workingFolder,
                buildFolder,
                architecture,
                installFolder,
                stagingFolder,
                headerFolder,
                stagingFolder,
                cmakeFlags,
                flavorFlags);

        if (hostOS == OSType.MacOS) {
            body("  echo Executing %s", command);
            body("  " + command);

            if (target != null && target.length() > 0) {
                body(String.format("echo %s --build %s --target %s -- -j8", cmakeExe, buildFolder, target));
                body(String.format("%s --build %s --target %s -- -j8", cmakeExe, buildFolder, target));
            } else {
                body(String.format("echo %s --build %s -- -j8", cmakeExe, buildFolder));
                body(String.format("%s --build %s -- -j8", cmakeExe, buildFolder));
            }
            body("  " + ABORT_LAST_FAILED);
            if (install) {
                body(String.format("%s --build %s --target install", cmakeExe, buildFolder));
                body("  " + ABORT_LAST_FAILED);
            }

            if (lib != null && lib.length() > 0) {
                String stagingLib = String.format("%s/lib/%s", stagingFolder, lib);
                body("  if [ -f '%s' ]; then", stagingLib);
                body("    mkdir -p %s/lib", redistFolder);
                body("    cp %s %s/lib/%s", stagingLib, redistFolder, lib);
                body("    " + ABORT_LAST_FAILED);
                body("  else");
                body("    echo CMAKEIFY ERROR: CMake build did not produce %s", stagingLib);
                body("    exit -100");
                body("  fi");
            }

            zips.put(zip.getAbsolutePath(), redistFolder.getPath());
            body("  if [ -d '%s' ]; then", stagingFolder);
            // Create a folder with something in it so there'e always something to zip
            body("    mkdir -p %s", redistFolder);
            bodyWithRedirect("    echo iOS %s %s  > %s/cmakeify.txt", cmakeVersion, platform, redistFolder);
            writeExtraIncludesToBody(includes, headerFolder);
            writeCreateZipFromRedistFolderToBody(zip, redistFolder);
            writeCreateHeaderZip(headers, headerFolder);
            writeZipFileStatisticsToBody(zip);

            if (lib == null || lib.length() > 0) {
                body("  else");
                body("    echo CMAKEIFY ERROR: Build did not produce an output in %s", stagingFolder);
                body("    exit -200");
            }
            body("  fi");

            // Still create the manifest for what would have been built.
            cdep("  - lib: %s", lib);
            cdep("    file: %s", zip.getName());
            cdep("    sha256: $SHASUM256");
            cdep("    size: $ARCHIVESIZE");
            if (multipleFlavor) {
                cdep("    flavor: %s", flavor);
            }
            cdep("    platform: %s", platform);
            cdep("    architecture: %s", architecture);
            cdep("    sdk: %s", sdk);
            if (multipleCMake) {
                cdep("    builder: cmake-%s", cmakeVersion);
            }
            body("fi");
        }
        return this;
    }

    private boolean isSupportediOSPlatformArchitecture(iOSPlatform platform, iOSArchitecture architecture) {
        if (platform.equals(iOSPlatform.iPhoneOS)) {
            if (architecture.equals(iOSArchitecture.arm64)) {
                return true;
            }
            if (architecture.equals(iOSArchitecture.armv7)) {
                return true;
            }
            return architecture.equals(iOSArchitecture.armv7s);
        }

        if (platform.equals(iOSPlatform.iPhoneSimulator)) {
            if (architecture.equals(iOSArchitecture.i386)) {
                return true;
            }
            return architecture.equals(iOSArchitecture.x86_64);
        }
        throw new RuntimeException(platform.toString());
    }

    private void writeCreateZipFromRedistFolderToBody(File zip, File folder) {
        body("  pushd %s", folder);
        body("  " + ABORT_LAST_FAILED);
        body("  zip %s . -r", zip);
        body("  " + ABORT_LAST_FAILED);
        body("  if [ -f '%s' ]; then", zip);
        body("    echo Zip %s was created", zip);
        body("  else");
        body("    echo CMAKEIFY ERROR: Zip %s was not created", zip);
        body("    exit -402");
        body("  fi");
        body("  popd");
        body("  " + ABORT_LAST_FAILED);
    }

    private void writeExtraIncludesToBody(String[] includes, File includesRedistFolder) {
        if (includes != null) {
            for (String include : includes) {
                body("  if [ ! -d '%s/%s' ]; then", workingFolder, include);
                body("    echo CMAKEIFY ERROR: Extra include folder '%s/%s' does not exist", workingFolder, include);
                body("    exit -600");
                body("  fi");
                body("  pushd %s", workingFolder);
                if (include.startsWith("include")) {
                    body("    echo find %s -name '*.h' {pipe} cpio -pdm %s", include, includesRedistFolder);
                    body("    find %s -name '*.h' | cpio -pdm %s", include, includesRedistFolder);
                    body("    echo find %s -name '*.hpp' {pipe} cpio -pdm %s", include, includesRedistFolder);
                    body("    find %s -name '*.hpp' | cpio -pdm %s", include, includesRedistFolder);
                } else {
                    body("    find %s -name '*.h' | cpio -pdm %s/include", include, includesRedistFolder);
                    body("    find %s -name '*.hpp' | cpio -pdm %s/include", include, includesRedistFolder);
                }
                body("  popd");
                body("  " + ABORT_LAST_FAILED);
            }
        }
    }

    @Override
    ScriptBuilder startBuilding(OS target) {
        switch (target) {
            case android:
                cdep("android:");
                cdep("  archives:");
                return this;
            case linux:
                cdep("linux:");
                cdep("  archives:");
                return this;
            case windows:
                cdep("windows:");
                cdep("  archives:");
                return this;
            case iOS:
                cdep("iOS:");
                cdep("  archives:");
                return this;
        }
        throw new RuntimeException(target.toString());
    }

    @Override
    ScriptBuilder buildRedistFiles(File workingFolder, String[] includes, String example) {
        if (!generateCDep()) {
            return this;
        }
        if (example != null && example.length() > 0) {
            cdep("example: |");
            String lines[] = example.split("\\r?\\n");
            for (String line : lines) {
                cdep("  %s", line);
            }
        }
        body("cat %s", cdepFile);

        body("echo - %s", new File(cdepFile.getParentFile(), "cdep-manifest" + fileSuffix + ".yml"));
        for (String zip : zips.keySet()) {
            String relativeZip = new File(".").toURI().relativize(new File(zip).toURI()).getPath();
            body("if [ -f '%s' ]; then", relativeZip);
            body("  echo - %s", relativeZip);
            body("fi");
        }
        return this;
    }

    @Override
    ScriptBuilder deployRedistFiles(
            RemoteArchive githubRelease,
            OS[] allTargets,
            boolean uploadBadges) {
        if (!generateCDep()) {
            return this;
        }

        File combinedManifest = new File(cdepFile.getParentFile(), "cdep-manifest" + fileSuffix + ".yml");
        File headers = new File(cdepFile.getParentFile(), filePrefix + "headers.zip");
        body("echo ${cdep} merge headers %s %s include %s", cdepFile, headers, cdepFile);
        body("${cdep} merge headers %s %s include %s", cdepFile, headers, cdepFile);
        body(ABORT_LAST_FAILED);

        if (targetVersion == null || targetVersion.length() == 0 || targetVersion.equals("0.0.0")) {
            body("echo Skipping upload because targetVersion='%s' %s", targetVersion, targetVersion.length());
            if (!combinedManifest.equals(cdepFile)) {
                body("# cdep-manifest" + fileSuffix + ".yml tracking: %s to %s", cdepFile, combinedManifest);
                body("cp %s %s", cdepFile, combinedManifest);
                body(ABORT_LAST_FAILED);
            } else {
                body("# cdep-manifest" + fileSuffix + ".yml tracking: not copying because it has the same name as combined");
                body("echo not copying %s to %s because it was already there. Still merge head", combinedManifest, cdepFile);
                body("ls %s", combinedManifest.getParent());
                body(ABORT_LAST_FAILED);
            }
            return this;
        }
        body("echo Not skipping upload because targetVersion='%s' %s", targetVersion, targetVersion.length());

        // Merging manifests from multiple travis runs is a PITA.
        // All runs need to upload cdep-manifest-[targetOS].yml.
        // The final run needs to figure out that it is the final run and also upload a merged
        // cdep-manifest.yml.
        // None of this needs to happen if specificTargetOS is null because that means there aren't
        // multiple travis runs.

        if (specificTargetOS != null) {
            assert !cdepFile.toString().endsWith("cdep-manifest.yml");
            if (allTargets.length == 1) {
                // There is a specificTargetOS specified but it is the only one.
                // We can combine the file locally.
                body("cp %s %s", cdepFile, combinedManifest);
                body(ABORT_LAST_FAILED);
                upload(headers, githubRelease);
                body(ABORT_LAST_FAILED);
                upload(combinedManifest, githubRelease);
                body(ABORT_LAST_FAILED);

            } else {
                // Accumulate a list of all targets to merge except for this one
                String otherCoordinates = "";
                for (OS os : allTargets) {
                    if (os != specificTargetOS) {
                        otherCoordinates += String.format("%s:%s/%s:%s ", targetGroupId, targetArtifactId, os, targetVersion);
                    }
                }

                // Now add this file
                String coordinates = otherCoordinates + cdepFile.toString();

                // Merge any existing manifest with the currently generated one.
                body("echo ${cdep} merge %s %s", coordinates, combinedManifest);
                body("${cdep} merge %s %s", coordinates, combinedManifest);
                body(ABORT_LAST_FAILED);

                // If the merge succeeded, that means we got all of the coordinates.
                // We can upload. Also need to fetch any partial dependencies so that
                // downstream calls to ./cdep for tests will have assets all ready.
                body("if [ -f '%s' ]; then", combinedManifest);
                body("  echo Fetching partial dependencies");
                body("  echo ${cdep} fetch %s", coordinates);
                body("  ${cdep} fetch %s", coordinates);
                body("  " + ABORT_LAST_FAILED);
                body("  echo Uploading %s", combinedManifest);
                upload(headers, githubRelease);
                body(ABORT_LAST_FAILED);
                upload(combinedManifest, githubRelease);
                body(ABORT_LAST_FAILED);
                if (uploadBadges) {
                    uploadBadges();
                }
                body("else");
                // If the merged failed then we still have to create a combined manifest for test
                // purposes but it won't be uploaded. Do the header merge at the same time as the
                // copy.
                body("  echo ${cdep} merge headers %s %s include %s", cdepFile, headers, combinedManifest);
                body("  ${cdep} merge headers %s %s include %s", cdepFile, headers, combinedManifest);
                body("  " + ABORT_LAST_FAILED);
                body("fi");

                // Upload the uncombined manifest
                upload(cdepFile, githubRelease);
            }
        } else {
            // There is not a specificTargetOS so there aren't multiple travis runs.
            // Just upload cdep-manifest.yml.
            assert cdepFile.toString().endsWith("cdep-manifest.yml");
            upload(headers, githubRelease);
            body(ABORT_LAST_FAILED);
            upload(cdepFile, githubRelease);
            body(ABORT_LAST_FAILED);
            if (uploadBadges) {
                uploadBadges();
            }
        }

        for (String zip : zips.keySet()) {
            String relativeZip = new File(".").toURI().relativize(new File(zip).toURI()).getPath();
            body("if [ -f '%s' ]; then", relativeZip);
            body("  echo Uploading %s", relativeZip);
            upload(new File(relativeZip), githubRelease);
            body("fi");
        }

        return this;
    }

    private void upload(File file, RemoteArchive githubRelease) {
        String user = targetGroupId.substring(targetGroupId.lastIndexOf(".") + 1);

        body("  echo %s/%s/github-release upload --user %s --repo %s --tag %s --name %s --file %s",
                TOOLS_FOLDER,
                getHostArchive(githubRelease).unpackroot,
                user,
                repo,
                targetVersion, file.getName(), file.getAbsolutePath());
        body("  %s/%s/github-release upload --user %s --repo %s --tag %s --name %s --file %s",
                TOOLS_FOLDER,
                getHostArchive(githubRelease).unpackroot,
                user,
                repo,
                targetVersion, file.getName(), file.getAbsolutePath());
        body(ABORT_LAST_FAILED);

    }

    private ScriptBuilder uploadBadges() {
        // Record build information
        String badgeUrl = String.format("%s:%s:%s", targetGroupId, targetArtifactId, targetVersion);
        badgeUrl = badgeUrl.replace(":", "%3A");
        badgeUrl = badgeUrl.replace("-", "--");
        badgeUrl = String.format("https://img.shields.io/badge/cdep-%s-brightgreen.svg", badgeUrl);
        String badgeFolder = String.format("%s/%s", targetGroupId, targetArtifactId);
        body("if [ -n \"$TRAVIS_TAG\" ]; then");
        body("  if [ -n \"$CDEP_BADGES_API_KEY\" ]; then");
        body("    echo git clone https://github.com/cdep-io/cdep-io.github.io.git");
        body("    git clone https://github.com/cdep-io/cdep-io.github.io.git");
        body("    " + ABORT_LAST_FAILED);
        body("    pushd cdep-io.github.io");
        body("    mkdir -p %s/latest", badgeFolder);
        bodyWithRedirect("    echo curl %s > %s/latest/latest.svg ", badgeUrl, badgeFolder);
        bodyWithRedirect("    curl %s > %s/latest/latest.svg ", badgeUrl, badgeFolder);
        body("    " + ABORT_LAST_FAILED);
        body("    echo git add %s/latest/latest.svg", badgeFolder);
        body("    git add %s/latest/latest.svg", badgeFolder);
        body("    " + ABORT_LAST_FAILED);
        body("    echo git -c user.name='cmakeify' -c user.email='cmakeify' commit -m init");
        body("    git -c user.name='cmakeify' -c user.email='cmakeify' commit -m init");
        body("    " + ABORT_LAST_FAILED);
        body("    echo git push -f -q https://cdep-io:$CDEP_BADGES_API_KEY@github.com/cdep-io/cdep-io.github.io");
        body("    git push -f -q https://cdep-io:$CDEP_BADGES_API_KEY@github.com/cdep-io/cdep-io.github.io");
        body("    " + ABORT_LAST_FAILED);
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
