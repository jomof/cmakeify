package com.jomofisher.cmakeify;

public class Configuration {
    enum OS {windows, linux, android};
    enum Compiler {gcc, clang}
    final public OS target[];
    final public Compiler compiler[];
    final public Gcc gcc;
    private Configuration() {
        target = null;
        compiler = null;
        gcc = null;
    }
}
