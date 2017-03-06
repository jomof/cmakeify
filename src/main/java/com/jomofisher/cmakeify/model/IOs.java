package com.jomofisher.cmakeify.model;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class IOs {

  final public String lib;
  final public Set<String> cmakeToolchains = new HashSet<>();
  final public Map<String, String> cmakeToolchainRemotes = new HashMap<>();

  IOs() {
    lib = "";
    cmakeToolchains.add("jomof-ios-cmake");
    cmakeToolchainRemotes.put("jomof-ios-cmake", "https://github.com/jomof/ios-cmake.git");
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (lib != null && lib.length() > 0) {
      sb.append(String.format("  lib: %s\n", lib));
    }
    sb.append("  cmakeToolchains: [");
    int i = 0;
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
