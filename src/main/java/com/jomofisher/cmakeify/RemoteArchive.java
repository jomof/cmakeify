package com.jomofisher.cmakeify;

public class RemoteArchive {
    final public ArchiveUrl linux;
    final public ArchiveUrl win32;
    final public ArchiveUrl win64;
    final public ArchiveUrl darwin;


    RemoteArchive() {
        linux = null;
        win32 = null;
        win64 = null;
        darwin = null;
    }

    RemoteArchive(ArchiveUrl linux, ArchiveUrl win32, ArchiveUrl win64, ArchiveUrl darwin) {
        this.linux = linux;
        this.win32 = win32;
        this.win64 = win64;
        this.darwin = darwin;
    }


    String toYaml(String prefix) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix + "linux: \n");
        sb.append(linux.toYaml(prefix + "  "));
        sb.append(prefix + "win32: \n");
        sb.append(win32.toYaml(prefix + "  "));
        sb.append(prefix + "win64: \n");
        sb.append(win64.toYaml(prefix + "  "));
        sb.append(prefix + "darwin: \n");
        sb.append(darwin.toYaml(prefix + "  "));
        return sb.toString();
    }
}
