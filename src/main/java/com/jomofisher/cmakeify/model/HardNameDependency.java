package com.jomofisher.cmakeify.model;

public class HardNameDependency {
    final public String compile;
    final public String sha256;
    private HardNameDependency() {
        this.compile = null;
        this.sha256 = null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("    - compile: %s\n", compile));
        sb.append(String.format("      sha256: %s\n", sha256));
        return super.toString();
    }
}
