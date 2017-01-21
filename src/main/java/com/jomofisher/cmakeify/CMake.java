package com.jomofisher.cmakeify;

public class CMake {
    final public CMakeVersion[] versions;
    CMake() {
        versions = new CMakeVersion[] {
            version(3, 7, 1),
            version(3, 7, 2)
        };
    }

    private static String productionPath(int major, int minor, int point, String os) {
          return String.format("http://cmake.org/files/v%s.%s/cmake-%s.%s.%s-%s.tar.gz",
                major, minor, major, minor, point, os);
    }

    private static String linuxPath(int major, int minor,int point) {
        return productionPath(major, minor, point, "Linux-x86_64");
    }

    private static String darwinPath(int major, int minor,int point) {
        return productionPath(major, minor, point, "Darwin-x86_64");
    }

    private CMakeVersion version(int major, int minor,int point) {
        return new CMakeVersion(linuxPath(major, minor, point), darwinPath(major, minor, point));
    }

    @Override
    public String toString() {
          StringBuilder sb = new StringBuilder();
          sb.append("  versions:\n");
            for (int i = 0; i < versions.length; ++i) {
                sb.append("    - linux: '" + versions[i].linux + "'\n");
                sb.append("      darwin: '" + versions[i].darwin + "'\n");
            }
        return sb.toString();
    }
}
