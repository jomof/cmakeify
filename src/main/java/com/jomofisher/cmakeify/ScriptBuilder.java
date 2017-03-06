package com.jomofisher.cmakeify;

import com.jomofisher.cmakeify.model.HardNameDependency;
import com.jomofisher.cmakeify.model.OS;
import com.jomofisher.cmakeify.model.RemoteArchive;
import com.jomofisher.cmakeify.model.Toolset;
import java.io.File;
import java.util.Collection;

abstract class ScriptBuilder {
    abstract File writeToShellScript();
    abstract ScriptBuilder startBuilding(OS target);
    abstract ScriptBuilder createEmptyBuildFolder(HardNameDependency dependencies[]);
    abstract ScriptBuilder uploadBadges();

    abstract ScriptBuilder download(RemoteArchive remote);

    abstract ScriptBuilder gitClone(String identifier, String repo);
    abstract ScriptBuilder checkForCompilers(Collection<String> compilers);
    abstract ScriptBuilder cmakeAndroid(
            String cmakeVersion,
            RemoteArchive cmakeRemote,
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
            Toolset toolset,
            boolean multipleCMake,
            boolean multipleGcc);

    abstract ScriptBuilder cmakeIOs(
        String cmakeVersion,
        String cmakeToolchainIdentifier,
        RemoteArchive cmakeRemote,
        boolean multipleCMake,
        boolean multipleCMakeToolchain);

    abstract ScriptBuilder buildRedistFiles(File workingFolder, String[] includes, String example);
}
