package com.genband.util.log.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;

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
  private String configFileName;
  private String kubernetesMasterUrl;
  private String serviceName;
  private String KUBERNETES_MASTER_URL_KEY;
  private int retries;
  private String[] topics;

  public String getConfigFileName() {
    return configFileName;
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
    loadProperties(null);
  }

  public ConfigManager(String configFileName) {
    loadProperties(configFileName);
  }

  public String getKubernetesMasterUrl() {
    return kubernetesMasterUrl;
  }

  /**
   * load all properties from the config file
   * 
   */
  private void loadProperties(String configFileName) {
    properties = new Properties();
    InputStream input = null;
    try {
      if (configFileName == null || "".equals(configFileName)) {
        configFileName = LogConfigConstants.default_log_path.toString();
      }
      input = Thread.currentThread().getContextClassLoader().getResourceAsStream(configFileName);

      // load a properties file
      properties.load(input);
      kubernetesMasterUrl =
          properties.getProperty(LogConfigConstants.kubernetes_master_url.toString());
      if (serviceName == null || "".equals(serviceName)) {
        this.serviceName = properties.getProperty(LogConfigConstants.service.toString());
      }
      this.retries = NumberUtils
          .isNumber(properties.getProperty(LogConfigConstants.connection_retry.toString()))
              ? NumberUtils.createInteger(
                  properties.getProperty(LogConfigConstants.connection_retry.toString()))
              : NumberUtils.createInteger(LogConfigConstants.connection_retry_default.toString());

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
