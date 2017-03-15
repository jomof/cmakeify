package com.jomofisher.cmakeify.model;

import java.util.HashMap;
import java.util.Map;

public class Ndk {
    final public String compilers[];
    final public String runtimes[];
    final public String versions[];
    final public String platforms[];
    final public String abis[];
    final public Map<String, RemoteArchive> remotes;
    final public String cppFlags;
    Ndk() {
        compilers = new String[] { "clang" };
        runtimes = new String[]{"c++"};
        versions = new String[] { "r13b" };
        platforms = new String[] { "21" };
        abis = new String[] { "armeabi", "armeabi-v7a", "arm64-v8a", "x86", "x86_64" };
        remotes = new HashMap<>();
        remotes.put("r13b", archiveUrlOf("r13b"));
        remotes.put("r14-beta1", archiveUrlOf("r14-beta1"));
        cppFlags = "";
    }

    private static RemoteArchive archiveUrlOf(String ndk) {
        String unpackRoot = "android-ndk-" + ndk;
        return new RemoteArchive(
            new ArchiveUrl(unpackRoot,
                "https://dl.google.com/android/repository/android-ndk-" + ndk + "-linux-x86_64.zip"),
            new ArchiveUrl(unpackRoot,
                "https://dl.google.com/android/repository/android-ndk-" + ndk + "-windows-i386.zip"),
            new ArchiveUrl(unpackRoot,
                "https://dl.google.com/android/repository/android-ndk-" + ndk + "-windows-x86_64.zip"),
            new ArchiveUrl(unpackRoot,
                "https://dl.google.com/android/repository/android-ndk-" + ndk + "-darwin-x86_64.zip"));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("    compilers: [");
        for (int j = 0; j < compilers.length; ++j) {
            if (j != 0) {
                sb.append(", ");
            }
            sb.append(compilers[j]);
        }
        sb.append("]\n");
        sb.append("    runtimes: [");
        for (int j = 0; j < runtimes.length; ++j) {
            if (j != 0) {
                sb.append(", ");
            }
            sb.append(runtimes[j]);
        }
        sb.append("]\n");
        sb.append("    versions: [");
        for (int j = 0; j < versions.length; ++j) {
            if (j != 0) {
                sb.append(", ");
            }
            sb.append(versions[j]);
        }
        sb.append("]\n");
        sb.append("    abis: [");
        for (int j = 0; j < abis.length; ++j) {
            if (j != 0) {
                sb.append(", ");
            }
            sb.append(abis[j]);
        }
        sb.append("]\n");
        sb.append("    platforms: [");
        for (int j = 0; j < platforms.length; ++j) {
            if (j != 0) {
                sb.append(", ");
            }
            sb.append(platforms[j]);
        }
        sb.append("]\n");
        if (remotes != null) {
            sb.append("    remotes:\n");
            for (String key : remotes.keySet()) {
                RemoteArchive remote = remotes.get(key);
                sb.append("      " + key + ":\n");
                sb.append(remote.toYaml("        "));
            }
        }
        return sb.toString();
    }

}
