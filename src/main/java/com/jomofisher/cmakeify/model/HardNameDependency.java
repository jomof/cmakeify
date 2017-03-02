package com.jomofisher.cmakeify.model;

public class HardNameDependency {
    final public String compile;
    final public String sha256;
    private HardNameDependency() {
        this.compile = null;
        this.sha256 = null;
    }
}
