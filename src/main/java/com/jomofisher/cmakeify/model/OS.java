package com.jomofisher.cmakeify.model;

public enum OS {
    windows,
    linux,
    android;

    String cmakeSystemName() {
        switch(this) {
            case android: return "Android";
            case linux: return "Linux";
            case windows: return "Windows";
        }
        throw new RuntimeException(this.toString());
    }
}
