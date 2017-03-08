package com.jomofisher.cmakeify.model;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class iOS {

  final public Map<String, String> flavors;
  final public String lib;
  final public List<iOSPlatform> platforms = new ArrayList<>();
  final public List<iOSArchitecture> architectures = new ArrayList<>();
  final public List<String> sdks = new ArrayList<>();

  iOS() {
    flavors = null;
    lib = "";
    for (iOSPlatform platform : iOSPlatform.values()) {
      platforms.add(platform);
    }
    for (iOSArchitecture architecture : iOSArchitecture.values()) {
      architectures.add(architecture);
    }
    sdks.add("10.2");
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
    sb.append("  architectures: [");
    i = 0;
    for (iOSArchitecture architecture : architectures) {
      if (i++ != 0) {
        sb.append(", ");
      }
      sb.append(architecture);
    }
    sb.append("]\n");
    sb.append("  sdks: [");
    i = 0;
    for (String sdk : sdks) {
      if (i++ != 0) {
        sb.append(", ");
      }
      sb.append(sdk);
    }
    sb.append("]\n");

    return sb.toString();
  }
}
