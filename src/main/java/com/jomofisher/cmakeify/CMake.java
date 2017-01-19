package com.jomofisher.cmakeify;

public class CMake {
    final public CMakeVersion[] versions;
    CMake() {
        versions = new CMakeVersion[] {
            new CMakeVersion(
                    "https://cmake.org/files/v3.7/cmake-3.7.2-Linux-x86_64.tar.gz",
                    "https://cmake.org/files/v3.7/cmake-3.7.2-Darwin-x86_64.tar.gz")
        };
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
