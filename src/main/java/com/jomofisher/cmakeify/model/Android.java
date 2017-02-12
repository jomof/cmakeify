package com.jomofisher.cmakeify.model;


public class Android {
    final public String lib;
    final public Ndk ndk;

    Android() {
        lib = "";
        ndk = new Ndk();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (lib != null && lib.length() > 0) {
            sb.append(String.format("lib: %s\n"));
        }
        sb.append("  ndk:\n");
        sb.append(ndk.toString());
        return sb.toString();
    }
}
