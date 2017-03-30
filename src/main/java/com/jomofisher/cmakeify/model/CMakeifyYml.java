package com.jomofisher.cmakeify.model;

public class CMakeifyYml {
  final public OS targets[];
  final public String buildTarget;
  final public String includes[];
  final public CMake cmake;
  final public Android android;
  final public Linux linux;
  final public com.jomofisher.cmakeify.model.iOS iOS;
  final public String example;
  final public boolean badges;
  final public CDep cdep;
  final public Releases releases;

  public CMakeifyYml() {
    targets = OS.values();
    buildTarget = null;
    includes = new String[0];
    cmake = new CMake();
    android = new Android();
    linux = new Linux();
    iOS = new iOS();
    example = null;
    badges = true;
    cdep = new CDep();
    releases = new Releases();
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
    if (buildTarget != null) {
      sb.append("buildTarget:\n");
      sb.append(buildTarget);
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
    if (iOS != null) {
      sb.append("iOS:\n");
      sb.append(iOS.toString());
    }
    if (cdep != null && cdep.dependencies != null && cdep.dependencies.length > 0) {
      sb.append("cdep:\n");
      sb.append(cdep.toString());
    }
    if (releases != null) {
      sb.append("releases:\n");
      sb.append(releases.toString());
    }
    return sb.toString();
  }
}
