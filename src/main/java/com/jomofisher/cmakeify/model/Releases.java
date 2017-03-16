package com.jomofisher.cmakeify.model;

public class Releases {
  final public RemoteArchive github;

  public Releases() {
    this.github = new RemoteArchive(
        new ArchiveUrl(
            "bin/linux/amd64",
            "https://github.com/aktau/github-release/releases/download/v0.6.2/linux-amd64-github-release.tar.bz2"),
        new ArchiveUrl(
            "bin/windows/amd64",
            "https://github.com/aktau/github-release/releases/download/v0.6.2/windows-amd64-github-release.zip"),
        new ArchiveUrl(
            "bin/windows/amd64",
            "https://github.com/aktau/github-release/releases/download/v0.6.2/windows-amd64-github-release.zip"),
        new ArchiveUrl(
            "bin/darwin/amd64",
            "https://github.com/aktau/github-release/releases/download/v0.6.2/darwin-amd64-github-release.tar.bz2")
    );
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("    github:\n");
    sb.append(github.toYaml("      "));
    return sb.toString();
  }
}
