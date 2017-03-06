package com.jomofisher.cmakeify.model;

import java.util.HashMap;
import java.util.Map;

public class CMake {
    final public String[] versions;
    final public Map<String, RemoteArchive> remotes;

    CMake() {
        versions = new String[] { "3.7.2" };
        remotes = new HashMap<>();
        remotes.put("3.7.1", remote(3, 7, 1));
        remotes.put("3.7.2", remote(3, 7, 2));
    }

    private static ArchiveUrl productionPath(int major, int minor, int point, String os,
        String extension, String unpackRootSuffix) {
          return new ArchiveUrl(
              String.format("cmake-%s.%s.%s-%s%s", major, minor, point, os, unpackRootSuffix),
              String.format("http://cmake.org/files/v%s.%s/cmake-%s.%s.%s-%s%s",
                major, minor, major, minor, point, os, extension));
    }

    private static ArchiveUrl linuxPath(int major, int minor,int point) {
        return productionPath(major, minor, point, "Linux-x86_64", ".tar.gz", "");
    }

    private static ArchiveUrl darwinPath(int major, int minor,int point) {
        return productionPath(major, minor, point, "Darwin-x86_64", ".tar.gz",
            "/CMake.app/Contents");
    }

    private static ArchiveUrl win32Path(int major, int minor,int point) {
        return productionPath(major, minor, point, "win32-x86", ".zip", "");
    }

    private static ArchiveUrl win64Path(int major, int minor,int point) {
        return productionPath(major, minor, point, "win64-x64", ".zip", "");
    }

    private RemoteArchive remote(int major, int minor,int point) {
        return new RemoteArchive(
                linuxPath(major, minor, point),
                win32Path(major, minor, point),
                win64Path(major, minor, point),
                darwinPath(major, minor, point));
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
                RemoteArchive remote = remotes.get(key);
                sb.append("      " + key + ":\n");
                sb.append(remote.toYaml("        "));
            }
        }
        return sb.toString();
    }
}
