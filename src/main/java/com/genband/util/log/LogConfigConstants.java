package com.genband.util.log;

public enum LogConfigConstants {
  default_log_path("config.properties"),
  log_file("log4j2.xml"),
  service("service"),
  topics("kafka.topics"),
  kubernetes_master_url_key("kubernetes.master.url.key"),
  kubernetes_master_url("kubernetes.master.url"),
  kubernetes_master_url_evn("KAFKA_LOGGING_SERVICE_HOST"),
  connection_retry("connection.retries"),
  connection_retry_default("3"),
  bootstrap_servers("bootstrap.servers"),
  pattern_layout("log.pattern.layout"),
  log_level("log.level"),
  kafka_dns("kafka.dns");
  
  private final String config;

  private LogConfigConstants(String config) {
    this.config = config;
  }

  public boolean equalsName(String otherConfig) {
    // (otherName == null) check is not needed because name.equals(null) returns false
    return config.equals(otherConfig);
  }

  @Override
  public String toString() {
    return this.config;
  }
}
