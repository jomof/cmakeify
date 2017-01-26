package com.jomofisher.cmakeify;


import java.io.File;

public class ArchiveInfo {
    public final String url; // Like https://cmake.org/files/v3.7/cmake-3.7.1-Linux-x86_64.tar.gz
    public final String name; // Like cmakeLinux-3.7.1-Linux-x86_64.tar.gz
    public final String extension; // Like .tar.gz
    public final String baseName; // Like cmakeLinux-3.7.1-Linux-x86_64
    ArchiveInfo(String url) {
        this.url = url;
        name = new File(url).getName();
        if (url.endsWith(".tar.gz")) {
            extension = ".tar.gz";
        } else if (url.endsWith(".zip")) {
            extension = ".zip";
        } else {
          throw new RuntimeException("Could not decode type of " + url);
        }
        baseName = this.name.substring(0, name.length() - extension.length());
    }

    public String downloadToFolder(String downloadFolder) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("if [ ! -f %s/%s ]; then\n", downloadFolder, name));
        sb.append(String.format("    echo Downloading %s\n", url));
        sb.append(String.format("    wget --no-check-certificate %s -O %s/%s > %s/%s.download-log 2>&1\n",
                url, downloadFolder, name, downloadFolder, name));
        sb.append("fi");
        return sb.toString();
    }

    public String uncompressToFolder(String downloadFolder, String toolsFolder) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("if [ ! -d %s/%s ]; then\n", toolsFolder, baseName));
        switch(extension) {
            case ".tar.gz":
                sb.append(String.format("    tar xvfz %s/%s -C %s > %s/%s.uncompress-log 2>&1\n",
                        downloadFolder, name, toolsFolder, toolsFolder, baseName));
                break;
            case ".zip":
                sb.append(String.format("    unzip %s/%s -d %s > %s/%s.uncompress-log 2>&1\n",
                        downloadFolder, name, toolsFolder, toolsFolder, baseName));
                break;
            default:
                throw new RuntimeException("Don't know how to uncompress " + name);
        }
        sb.append("fi");
        return sb.toString();
    }
}
