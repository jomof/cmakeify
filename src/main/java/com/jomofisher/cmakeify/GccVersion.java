package com.jomofisher.cmakeify;

public class GccVersion {
    final public String c;
    final public String cxx;
    GccVersion() {
        c = null;
        cxx = null;
    }
    GccVersion(String c, String cxx) {
        this.c = c;
        this.cxx = cxx;
    }
}
