package com.jomofisher.cmakeify.model;

public enum OS {
    windows,
    linux,
    android,
    iOS;

    String cmakeSystemName() {
        switch(this) {
            case android: return "Android";
            case linux: return "Linux";
            case iOS:
                return null; // No System name for iOS
        }
        throw new RuntimeException(this.toString());
    }
}
