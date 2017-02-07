package com.genband.util.log.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.springframework.util.ResourceUtils;

import com.genband.util.log.constants.LogConfigConstants;

/**
 * this class is configuration for kubernetes connection. Any other configuration of services on k8s
 * like kafka, you can extend the abstract class.
 * 
 * @author dixiao
 *
 */
public abstract class ConfigManager {

  protected Properties properties = null;
  private String configFilePath;
  private String kubernetesMasterUrl;
  private String serviceName;
  private String KUBERNETES_MASTER_URL_KEY;
  private int retries;
  private String patternLayout = null;
  private String[] topics;

  public String getPatternLayout() {
    return patternLayout;
  }

  public String getKUBERNETES_MASTER_URL_KEY() {
    return KUBERNETES_MASTER_URL_KEY;
  }

  public int getRetries() {
    return retries;
  }

  public String[] getTopics() {
    return topics;
  }

  abstract public HashMap<String, String> getLabelMap();

  abstract public Properties getProperties();

  abstract public void setProperties(Properties properties);

  private final Logger logger = Logger.getLogger(ConfigManager.class);

  public ConfigManager() {
    loadProperties();
  }

  public ConfigManager(String configFilePath) {
    try {
      this.configFilePath = ResourceUtils.getFile("classpath:" + configFilePath).getAbsolutePath();
    } catch (FileNotFoundException e) {
      logger.error("Cannot find config.properties on classpath");
      logger.error(e);
    }
    loadProperties();
  }

  public String getKubernetesMasterUrl() {
    return kubernetesMasterUrl;
  }

  /**
   * load all properties from the config file
   * 
   */
  private void loadProperties() {
    properties = new Properties();
    InputStream input = null;
    try {
      if (configFilePath == null || "".equals(configFilePath)) {
        configFilePath = LogConfigConstants.default_log_path.toString();
      }
      input = new FileInputStream(configFilePath);
      // load a properties file
      properties.load(input);
      kubernetesMasterUrl =
          properties.getProperty(LogConfigConstants.kubernetes_master_url.toString());
      if (serviceName == null || "".equals(serviceName)) {
        this.serviceName = properties.getProperty(LogConfigConstants.service.toString());
      }
      this.KUBERNETES_MASTER_URL_KEY =
          properties.getProperty(LogConfigConstants.kubernetes_master_url_key.toString());
      this.retries = NumberUtils
          .isNumber(properties.getProperty(LogConfigConstants.connection_retry.toString()))
              ? NumberUtils.createInteger(
                  properties.getProperty(LogConfigConstants.connection_retry.toString()))
              : NumberUtils.createInteger(LogConfigConstants.connection_retry_default.toString());
      this.patternLayout = properties.getProperty(LogConfigConstants.pattern_layout.toString());
      topics = properties.getProperty(LogConfigConstants.topics.toString()).split(",");
    } catch (IOException ex) {
      logger.error(ex);
    } finally {
      if (input != null) {
        try {
          input.close();
        } catch (IOException e) {
          logger.error(e);
        }
      }
    }
  }

}
