package com.jomofisher.cmakeify.model;

import java.util.Map;

public class Android {
    final public Map<String, String> flavors;
    final public String lib;
    final public Ndk ndk;

    Android() {
        flavors = null;
        lib = "";
        ndk = new Ndk();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (flavors != null && flavors.size() > 0) {
            sb.append("  flavors:\n");
            for (String flavor : flavors.keySet()) {
                String flags = flavors.get(flavor);
                if (flags != null) {
                    sb.append(String.format("    %s: %s\n", flavor, flags));
                }
            }
        }
        if (lib != null && lib.length() > 0) {
            sb.append(String.format("  lib: %s\n", lib));
        }
        sb.append("  ndk:\n");
        sb.append(ndk.toString());
        return sb.toString();
    }
}
