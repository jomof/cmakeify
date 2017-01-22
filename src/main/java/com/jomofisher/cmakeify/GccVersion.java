package com.jomofisher.cmakeify;

public class GccVersion {
    final public String c;
    final public String cxx;
    final public OS target;
    GccVersion() {
        c = null;
        cxx = null;
        target = null;
    }
    GccVersion(String c, String cxx, OS target) {
        this.c = c;
        this.cxx = cxx;
        this.target = target;
    }
}
