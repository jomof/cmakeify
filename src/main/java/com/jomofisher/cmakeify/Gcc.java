package com.jomofisher.cmakeify;

public class Gcc {
    public final GccVersion[] versions;
    Gcc() {
        this.versions = new GccVersion[] {
                new GccVersion("gcc-4.9", "g++-4.9"),
                new GccVersion("gcc-5", "g++-5"),
                new GccVersion("gcc-6", "g++-6"),
                new GccVersion("gcc-mingw-w64-x86-64", "gcc-mingw-w64-x86-64")};
    }

    @Override
    public String toString() {
          StringBuilder sb = new StringBuilder();
          sb.append("  versions:\n");
            for (int i = 0; i < versions.length; ++i) {
                sb.append("    - c: '" + versions[i].c + "'\n");
                sb.append("      cxx: '" + versions[i].cxx + "'\n");
            }
        return sb.toString();
    }
}
