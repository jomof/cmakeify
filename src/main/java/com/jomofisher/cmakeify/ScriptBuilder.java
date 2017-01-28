package com.jomofisher.cmakeify;

import java.io.File;
import java.util.Collection;

abstract class ScriptBuilder {
    abstract File writeToShellScript();
    abstract ScriptBuilder createEmptyBuildFolder(File workingFolder);
    abstract ScriptBuilder download(RemoteArchive remote);
    abstract ScriptBuilder checkForCompilers(Collection<String> compilers);
    abstract ScriptBuilder cmakeAndroid(
            File workingDirectory,
            String cmakeVersion,
            RemoteArchive cmakeRemote,
            String ndkVersion,
            RemoteArchive ndkRemote,
            String platform,
            String abi,
            boolean multipleCMake,
            boolean multipleNDK,
            boolean multiplePlatforms);
    abstract ScriptBuilder cmakeLinux(
            File workingDirectory,
            String cmakeVersion,
            RemoteArchive cmakeRemote,
            Toolset toolset,
            boolean multipleCMake,
            boolean multipleGcc);
    abstract ScriptBuilder buildRedistFiles();
}
