package com.jomofisher.cmakeify.model;


public class Android {
    final public Ndk ndk;

    Android() {
        ndk = new Ndk();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("  ndk:\n");
        sb.append(ndk.toString());
        return sb.toString();
    }
}
