package com.jomofisher.cmakeify;

public class Gcc {
    public final GccVersion[] versions;
    Gcc() {
        this.versions = new GccVersion[] {
                new GccVersion("gcc-4.9", "g++-4.9", OS.linux),
                new GccVersion("gcc-5", "g++-5", OS.linux),
                new GccVersion("gcc-6", "g++-6", OS.linux),
                new GccVersion("x86_64-w64-mingw32-gcc-4.6", "x86_64-w64-mingw32-gcc-4.6", OS.linux.windows)};
    }

    @Override
    public String toString() {
          StringBuilder sb = new StringBuilder();
          sb.append("  versions:\n");
            for (int i = 0; i < versions.length; ++i) {
                sb.append("    - c: '" + versions[i].c + "'\n");
                sb.append("      cxx: '" + versions[i].cxx + "'\n");
                sb.append("      target: " + versions[i].target + "\n");
            }
        return sb.toString();
    }
}
