package com.jomofisher.cmakeify;

import java.util.HashMap;
import java.util.Map;

public class Ndk {
    final public String versions[];
    final public Map<String, Remote> remotes;
    Ndk() {
        versions = new String[] { "r13b" };
        remotes = new HashMap<>();
        remotes.put("r13b", new Remote(
                "https://dl.google.com/android/repository/android-ndk-r13b-linux-x86_64.zip",
                "https://dl.google.com/android/repository/android-ndk-r13b-windows-x86.zip",
                "https://dl.google.com/android/repository/android-ndk-r13b-windows-x86_64.zip",
                "https://dl.google.com/android/repository/android-ndk-r13b-linux-x86_64.zip"));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("    versions: [");
        for (int j = 0; j < versions.length; ++j) {
            if (j != 0) {
                sb.append(", ");
            }
            sb.append(versions[j]);
        }
        sb.append("]\n");
        if (remotes != null) {
            sb.append("    remotes:\n");
            for (String key : remotes.keySet()) {
                Remote remote = remotes.get(key);
                sb.append("      " + key + ":\n");
                sb.append("        linux: " + remote.linux + "\n");
                sb.append("        win32: " + remote.win32 + "\n");
                sb.append("        win64: " + remote.win64 + "\n");
                sb.append("        darwin: " + remote.darwin + "\n");
            }
        }
        return sb.toString();
    }

}
