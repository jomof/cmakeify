package com.jomofisher.cmakeify;

import java.io.File;
import java.util.Collection;

abstract class ScriptBuilder {
    abstract File writeToShellScript();
    abstract ScriptBuilder createToolsFolder();
    abstract ScriptBuilder createDownloadsFolder();
    abstract ScriptBuilder download(Remote remote);
    abstract ScriptBuilder checkForCompilers(Collection<String> compilers);
    abstract ScriptBuilder cmakeAndroid(
            File workingDirectory,
            String cmakeVersion,
            Remote cmakeRemote,
            String ndkVersion,
            Remote ndkRemote,
            String abi,
            boolean multipleCMake,
            boolean multipleNDK);
    abstract ScriptBuilder cmakeLinux(
            File workingDirectory,
            String cmakeVersion,
            Remote cmakeRemote,
            Toolset toolset,
            boolean multipleCMake,
            boolean multipleGcc);
}
