package com.jomofisher.cmakeify;

public class Configuration {
    enum OS {windows, linux, android};
    enum Compiler {gcc, clang}
    final public OS target[];
    final public Compiler compiler[];
    final public Gcc gcc;
    private Configuration() {
        target = OS.values();
        compiler = Compiler.values();
        gcc = new Gcc();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("target: ");
        for (int j = 0; j < target.length; ++j) {
            if (j != 0) {
                sb.append(", ");
            }
            sb.append(target[j]);
        }
        sb.append("\n");

        sb.append("compiler: ");
        for (int j = 0; j < compiler.length; ++j) {
            if (j != 0) {
                sb.append(", ");
            }
            sb.append(compiler[j]);
        }
        sb.append("\n");
        sb.append("gcc:\n");
        sb.append(gcc.toString());
        return sb.toString();
    }
}
