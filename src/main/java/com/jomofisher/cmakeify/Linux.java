package com.jomofisher.cmakeify;

import java.util.HashMap;
import java.util.Map;

public class Linux {
    final public String compilers[];
    final public Map<String, Toolset> toolsets;

    Linux() {
        compilers = new String[] { "gcc-4.8" };
        toolsets = new HashMap<>();
        toolsets.put("gcc-4.8", new Toolset("gcc-4.8", "g++-4.8"));
        toolsets.put("gcc-4.9", new Toolset("gcc-4.9", "g++-4.9"));
        toolsets.put("gcc-5", new Toolset("gcc-5", "g++-5"));
        toolsets.put("gcc-6", new Toolset("gcc-6", "g++-6"));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("    compilers: [");
        for (int j = 0; j < compilers.length; ++j) {
            if (j != 0) {
                sb.append(", ");
            }
            sb.append(compilers[j]);
        }
        sb.append("]\n");
        if (toolsets != null) {
            sb.append("    toolsets:\n");
            for (String key : toolsets.keySet()) {
                Toolset toolset = toolsets.get(key);
                sb.append("      " + key + ":\n");
                sb.append("        c: " + toolset.c + "\n");
                sb.append("        cxx: " + toolset.cxx + "\n");
            }
        }
        return sb.toString();
    }

}
