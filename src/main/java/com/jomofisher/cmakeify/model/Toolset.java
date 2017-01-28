package com.jomofisher.cmakeify.model;

public class Toolset {
    final public String c;
    final public String cxx;
    Toolset() {
        c = null;
        cxx = null;
    }
    Toolset(String c, String cxx) {
        this.c = c;
        this.cxx = cxx;
    }
}
