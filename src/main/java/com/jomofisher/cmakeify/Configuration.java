package com.jomofisher.cmakeify;

public class Configuration {
    enum OS {windows, linux, android};
    enum Compiler {gcc, clang}
    final public OS targets[];
    final public Compiler compilers[];
    final public Gcc gcc;
    final public CMake cmake;
    Configuration() {
        targets = OS.values();
        compilers = Compiler.values();
        gcc = new Gcc();
        cmake = new CMake();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("targets: [");
        for (int j = 0; j < targets.length; ++j) {
            if (j != 0) {
                sb.append(", ");
            }
            sb.append(targets[j]);
        }
        sb.append("]\n");

        sb.append("compilers: [");
        for (int j = 0; j < compilers.length; ++j) {
            if (j != 0) {
                sb.append(", ");
            }
            sb.append(compilers[j]);
        }
        sb.append("]\n");
        sb.append("gcc:\n");
        sb.append(gcc.toString());
        if (cmake != null) {
            sb.append("cmake:\n");
            sb.append(cmake.toString());
        }
        return sb.toString();
    }
}
