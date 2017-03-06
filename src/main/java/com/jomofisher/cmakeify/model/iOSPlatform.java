package com.jomofisher.cmakeify.model;

public enum iOSPlatform {
  iPhone("OS"),
  simulator("SIMULATOR"),
  simulator64("SIMULATOR64");

  final public String cmakeCode;

  iOSPlatform(String cmakeCode) {
    this.cmakeCode = cmakeCode;
  }
}
