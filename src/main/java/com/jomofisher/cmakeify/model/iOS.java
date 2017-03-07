package com.jomofisher.cmakeify.model;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class iOS {

  final public Map<String, String> flavors;
  final public String lib;
  final public List<iOSPlatform> platforms = new ArrayList<>();
  final public Set<String> cmakeToolchains = new HashSet<>();
  final public Map<String, String> cmakeToolchainRemotes = new HashMap<>();

  iOS() {
    flavors = null;
    lib = "";
    platforms.add(iOSPlatform.iPhone);
    platforms.add(iOSPlatform.simulator);
    platforms.add(iOSPlatform.simulator64);

    cmakeToolchains.add("jomof-ios-cmake");
    cmakeToolchainRemotes.put("jomof-ios-cmake", "https://github.com/jomof/ios-cmake.git");
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (flavors != null && flavors.size() > 0) {
      sb.append("  flavors:\n");
      for (String flavor : flavors.keySet()) {
        String flags = flavors.get(flavor);
        if (flags != null) {
          sb.append(String.format("    %s: %s\n", flavor, flags));
        }
      }
    }
    if (lib != null && lib.length() > 0) {
      sb.append(String.format("  lib: %s\n", lib));
    }
    sb.append("  platforms: [");
    int i = 0;
    for (iOSPlatform platform : platforms) {
      if (i++ != 0) {
        sb.append(", ");
      }
      sb.append(platform);
    }
    sb.append("]\n");
    sb.append("  cmakeToolchains: [");
    i = 0;
    for (String cmakeToolchain : cmakeToolchains) {
      if (i++ != 0) {
        sb.append(", ");
      }
      sb.append(cmakeToolchain);
    }
    sb.append("]\n");
    if (cmakeToolchainRemotes != null && cmakeToolchainRemotes.size() > 0) {
      sb.append("  cmakeToolchainRemotes:\n");
      for (String name : cmakeToolchainRemotes.keySet()) {
        String repo = cmakeToolchainRemotes.get(name);
        if (repo != null) {
          sb.append(String.format("    %s: %s\n", name, repo));
        }
      }
    }
    return sb.toString();
  }
}