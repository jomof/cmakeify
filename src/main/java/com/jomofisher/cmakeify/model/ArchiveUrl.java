package com.jomofisher.cmakeify.model;

public class ArchiveUrl {
  public String unpackroot;
  public String url;

  ArchiveUrl() {
    unpackroot = null;
    url = null;
  }

  public ArchiveUrl(String unpackroot, String url) {
    this.unpackroot = unpackroot;
    this.url = url;
  }

  String toYaml(String prefix) {
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("%sunpackroot: %s\n", prefix, unpackroot));
    sb.append(String.format("%surl: %s\n", prefix, url));
    return sb.toString();
  }
}
