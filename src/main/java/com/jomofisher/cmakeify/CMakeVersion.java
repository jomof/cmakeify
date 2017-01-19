package com.jomofisher.cmakeify;

public class CMakeVersion {
    final public String linux;
    final public String darwin;
    CMakeVersion() {
        this.linux = null;
        this.darwin = null;
    }
    CMakeVersion(String linux, String darwin) {
        this.linux = linux;
        this.darwin = darwin;
    }
}
