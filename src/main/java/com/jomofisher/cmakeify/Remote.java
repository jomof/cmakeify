package com.jomofisher.cmakeify;

public class Remote {
    final public String linux;
    final public String win32;
    final public String win64;
    final public String darwin;
    Remote() {
        linux = null;
        win32 = null;
        win64 = null;
        darwin = null;
    }
    Remote(String linux, String win32, String win64, String darwin) {
        this.linux = linux;
        this.win32 = win32;
        this.win64 = win64;
        this.darwin = darwin;
    }
}
