package com.jomofisher.cmakeify;

import java.io.File;

abstract class ScriptBuilder {
    abstract ScriptBuilder createToolsFolder();
    abstract ScriptBuilder createDownloadsFolder();
    abstract ScriptBuilder downloadCMake(CMakeVersion version);
    abstract File writeToShellScript(File workingFolder);
}
