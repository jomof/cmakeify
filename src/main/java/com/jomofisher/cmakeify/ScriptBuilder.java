package com.jomofisher.cmakeify;

import com.jomofisher.cmakeify.model.RemoteArchive;
import com.jomofisher.cmakeify.model.Toolset;

import java.io.File;
import java.util.Collection;

abstract class ScriptBuilder {
    abstract File writeToShellScript();
    abstract ScriptBuilder createEmptyBuildFolder();
    abstract ScriptBuilder download(RemoteArchive remote);
    abstract ScriptBuilder checkForCompilers(Collection<String> compilers);
    abstract ScriptBuilder cmakeAndroid(
            String cmakeVersion,
            RemoteArchive cmakeRemote,
            String ndkVersion,
            RemoteArchive ndkRemote,
            String compiler,
            String platform,
            String abi,
            boolean multipleCMake,
            boolean multipleNDK,
            boolean multipleCompiler,
            boolean multiplePlatforms);
    abstract ScriptBuilder cmakeLinux(
            String cmakeVersion,
            RemoteArchive cmakeRemote,
            Toolset toolset,
            boolean multipleCMake,
            boolean multipleGcc);
    abstract ScriptBuilder buildRedistFiles(File workingFolder);
}
