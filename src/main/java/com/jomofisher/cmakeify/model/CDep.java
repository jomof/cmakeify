package com.jomofisher.cmakeify.model;


public class CDep {
    final public HardNameDependency dependencies[];
    CDep() {
      this.dependencies = new HardNameDependency[0];
    }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (dependencies != null && dependencies.length > 0) {
      sb.append("  dependencies:\n");
      for (HardNameDependency dependency : dependencies) {
        sb.append(String.format("  - %s\n", dependency.toString()));
      }
    }
    return sb.toString();
  }
}
