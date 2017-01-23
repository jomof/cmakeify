package com.jomofisher.cmakeify;

import java.io.File;
import java.util.Collection;
import java.util.Set;

abstract class ScriptBuilder {
    abstract File writeToShellScript();
    abstract ScriptBuilder createToolsFolder();
    abstract ScriptBuilder createDownloadsFolder();
    abstract ScriptBuilder downloadCMake(CMakeVersion version);
    abstract ScriptBuilder checkForCompilers(Collection<String> compilers);
    abstract ScriptBuilder cmake(File workingDirectory, CMakeVersion cmake);
}
