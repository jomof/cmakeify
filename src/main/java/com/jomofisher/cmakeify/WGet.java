package com.jomofisher.cmakeify;

import java.io.File;

public class WGet extends ScriptNode {
    final public String remote;
    final public File local;
    WGet(String remote, File local) {
        this.remote = remote;
        this.local = local;
    }
}
