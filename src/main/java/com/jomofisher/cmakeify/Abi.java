package com.jomofisher.cmakeify;

/**
 * Enum of valid ABI you can specify for NDK.
 */
public enum Abi {
  ARMEABI("armeabi", "arm"),
  ARMEABI_V7A("armeabi-v7a", "arm"),
  ARM64_V8A("arm64-v8a", "arm64"),
  X86("x86", "x86"),
  X86_64("x86_64", "x86_64");

  private final String name;
  private final String architecture;

  private Abi(String name, String architecture) {
    this.name = name;
    this.architecture = architecture;

  }

  /**
   * Returns the ABI Enum with the specified name.
   */
  public static Abi getByName(String name) {
    for (Abi abi : values()) {
      if (abi.name.equals(name)) {
        return abi;
      }
    }
    throw new RuntimeException(name);
  }

  /**
   * Returns name of the ABI like "armeabi-v7a".
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the CPU architecture like "arm".
   */
  public String getArchitecture() {
    return architecture;
  }
}
