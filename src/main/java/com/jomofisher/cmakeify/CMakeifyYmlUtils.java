package com.jomofisher.cmakeify;

import com.jomofisher.cmakeify.model.CMakeifyYml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CMakeifyYmlUtils {
    public static List<String> buildTargets(CMakeifyYml model) {
        List<String> result = new ArrayList<>();
        if (model.buildTarget != null) {
            result.add(model.buildTarget);
        }
        if (model.buildTargets != null) {
            Collections.addAll(result, model.buildTargets);
        }
        return result;
    }
    public static void validateModel(CMakeifyYml model) {
        if (model.android != null) {
            if (model.android.ndk == null) {
                throw new RuntimeException("cmakeify.yml had android block but no android.ndk");
            }
            if (model.android.ndk.runtimes == null) {
                throw new RuntimeException("cmakeify.yml had android.ndk block but no android.ndk.runtimes");
            }
            for (String runtime : model.android.ndk.runtimes) {
                switch (runtime) {
                    case "c++":
                    case "stlport":
                    case "gnustl":
                        break;
                    default:
                        throw new RuntimeException(String.format("" +
                                "Unexpected runtime '%s'. Allowed: c++, stlport, gnustl", runtime));
                }
            }
        }
    }
}
