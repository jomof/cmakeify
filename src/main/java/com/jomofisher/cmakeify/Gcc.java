package com.jomofisher.cmakeify;

public class Gcc {
    public final String[] versions;
    Gcc() {
        this.versions = new String[] {"4.9.0"};
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("  versions: ");
        for (int j = 0; j < versions.length; ++j) {
            if (j != 0) {
                sb.append(", ");
            }
            sb.append("'" + versions[j] + "'");
        }
        sb.append("\n");
        return sb.toString();
    }
}
