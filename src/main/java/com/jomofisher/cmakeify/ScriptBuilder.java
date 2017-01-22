package com.jomofisher.cmakeify;

import java.io.File;
import java.util.Collection;
import java.util.Set;

abstract class ScriptBuilder {
    abstract ScriptBuilder createToolsFolder();
    abstract ScriptBuilder createDownloadsFolder();
    abstract ScriptBuilder downloadCMake(CMakeVersion version);
    abstract File writeToShellScript(File workingFolder);
    abstract ScriptBuilder installPackages(Collection<String> packages);
}
