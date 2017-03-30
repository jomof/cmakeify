package com.jomofisher.cmakeify;

import com.jomofisher.cmakeify.model.*;

import java.io.File;
import java.util.Collection;

abstract class ScriptBuilder {

  abstract File writeToShellScript();

  abstract ScriptBuilder startBuilding(OS target);

  abstract ScriptBuilder createEmptyBuildFolder(HardNameDependency dependencies[]);

  abstract ScriptBuilder download(RemoteArchive remote);

  abstract ScriptBuilder checkForCompilers(Collection<String> compilers);

  abstract ScriptBuilder cmakeAndroid(
      String cmakeVersion,
      RemoteArchive cmakeRemote,
      String target,
      String cmakeFlags,
      String androidCppFlags,
      String flavor,
      String flavorFlags,
      String ndkVersion,
      RemoteArchive ndkRemote,
      String includes[],
      String lib,
      String compiler,
      String runtime,
      String platform,
      String abi[],
      boolean multipleFlavors,
      boolean multipleCMake,
      boolean multipleNDK,
      boolean multipleCompiler,
      boolean multipleRuntime,
      boolean multiplePlatforms);

  abstract ScriptBuilder cmakeLinux(
      String cmakeVersion,
      RemoteArchive cmakeRemote,
      String target,
      String cmakeFlags,
      Toolset toolset,
      String lib,
      boolean multipleCMake,
      boolean multipleGcc);

  abstract ScriptBuilder cmakeiOS(
      String cmakeVersion,
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
      boolean multipleSdk);

  abstract ScriptBuilder buildRedistFiles(File workingFolder, String[] includes, String example);

  abstract ScriptBuilder deployRedistFiles(
      RemoteArchive githubRelease,
      OS[] allTargets,
      boolean uploadBadges);
}
