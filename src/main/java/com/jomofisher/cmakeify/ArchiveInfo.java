package com.jomofisher.cmakeify;


import java.io.File;

public class ArchiveInfo {
    public final String url; // Like https://cmake.org/files/v3.7/cmake-3.7.1-Linux-x86_64.tar.gz
    public final String name; // Like cmake-3.7.1-Linux-x86_64.tar.gz
    public final String extension; // Like .tar.gz
    public final String baseName; // Like cmake-3.7.1-Linux-x86_64
    ArchiveInfo(String url) {
        this.url = url;
        name = new File(url).getName();
        if (url.endsWith(".tar.gz")) {
            extension = ".tar.gz";
        } else {
          throw new RuntimeException("Could not decode type of " + url);
        }
        baseName = this.name.substring(0, name.length() - extension.length());
    }

    public String downloadToFolder(String downloadFolder) {
        return String.format("wget %s -O %s/%s", url, downloadFolder, name);
    }

    public String uncompressToFolder(String downloadFolder, String toolsFolder) {
        return String.format("tar xvfz %s/%s -C %s", downloadFolder, name, toolsFolder);
    }
}
