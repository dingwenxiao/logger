package com.genband.util.log;

public enum Topics {
  info("info"), trace("trace"), debug("debug"), error("error"), warn("warn");

  private final String level;

  private Topics(String level) {
    this.level = level;
  }

  public boolean equals(String otherLevel) {
    return this.level.equals(otherLevel);
  }

  @Override
  public String toString() {
    return this.level;
  }
}
