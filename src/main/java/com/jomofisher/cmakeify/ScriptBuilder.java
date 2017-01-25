package com.jomofisher.cmakeify;

import java.io.File;
import java.util.Collection;

abstract class ScriptBuilder {
    abstract File writeToShellScript();
    abstract ScriptBuilder createToolsFolder();
    abstract ScriptBuilder createDownloadsFolder();
    abstract ScriptBuilder download(Remote remote);
    abstract ScriptBuilder checkForCompilers(Collection<String> compilers);
    abstract ScriptBuilder cmake(
            File workingDirectory,
            String cmakeVersion,
            Remote cmakeRemote,
            GccVersion gccVersion,
            boolean multipleCMake,
            boolean multipleGcc);
}
