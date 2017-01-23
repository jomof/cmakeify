package com.jomofisher.cmakeify;

public class CMakeVersion {
    final public String tag;
    final public String linux;
    final public String darwin;
    CMakeVersion() {
        this.tag = null;
        this.linux = null;
        this.darwin = null;
    }
    CMakeVersion(String tag, String linux, String darwin) {
        this.tag = tag;
        this.linux = linux;
        this.darwin = darwin;
    }
}
