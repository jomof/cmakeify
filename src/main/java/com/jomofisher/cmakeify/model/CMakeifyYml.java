package com.jomofisher.cmakeify.model;

public class CMakeifyYml {
    final public OS targets[];
    final public String includes[];
    final public CMake cmake;
    final public Android android;
    final public Linux linux;
    final public com.jomofisher.cmakeify.model.iOS iOS;
    final public String example;
    final public boolean badges;
    final public CDep cdep;

    public CMakeifyYml() {
        targets = OS.values();
        includes = new String[0];
        cmake = new CMake();
        android = new Android();
        linux = new Linux();
        iOS = new iOS();
        example = null;
        badges = true;
        cdep = new CDep();
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
        if (includes != null && includes.length > 0) {
            sb.append("includes: [");
            for (int j = 0; j < includes.length; ++j) {
                if (j != 0) {
                    sb.append(", ");
                }
                sb.append(includes[j]);
            }
            sb.append("]\n");
        }
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
        if (android != null) {
            sb.append("iOS:\n");
            sb.append(iOS.toString());
        }
        return sb.toString();
    }
}
