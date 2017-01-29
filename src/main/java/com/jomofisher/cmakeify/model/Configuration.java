package com.jomofisher.cmakeify.model;

public class Configuration {
    final public OS targets[];
    final public CMake cmake;
    final public Android android;
    final public Linux linux;
    public Configuration() {
        targets = OS.values();
        cmake = new CMake();
        android = new Android();
        linux = new Linux();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("targets: [");
        for (int j = 0; j < targets.length; ++j) {
            if (j != 0) {
                sb.append(", ");
            }
            sb.append(targets[j]);
        }
        sb.append("]\n");

        if (cmake != null) {
            sb.append("cmake:\n");
            sb.append(cmake.toString());
        }
        if (linux != null) {
            sb.append("linux:\n");
            sb.append(linux.toString());
        }
        if (android != null) {
            sb.append("android:\n");
            sb.append(android.toString());
        }
        return sb.toString();
    }
}
