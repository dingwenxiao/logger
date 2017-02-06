package com.genband.util.log.config;

import java.util.HashMap;
import java.util.Properties;

/**
 * This class is for loading kafka service configuration such kafka service label.
 * 
 * @author dixiao
 *
 */
public class KafkaConfigManager extends ConfigManager {

  private HashMap<String, String> kafkaLabelsMap = new HashMap<>();

  /**
   * Constructor for kafka configuration class
   * 
   * @param configPath
   */
  public KafkaConfigManager(String configPath) {
    super(configPath);
    loadKafkaLabelMap();
  }

  /**
   * Get label map from configuration
   */
  @Override
  public HashMap<String, String> getLabelMap() {
    return kafkaLabelsMap;
  }

  /**
   * Assign values to label map
   */
  private void loadKafkaLabelMap() {
    for (String key : properties.stringPropertyNames()) {
      if (key.startsWith("kafka.label")) {
        String value = properties.getProperty(key);
        kafkaLabelsMap.put(key, value);
      }
    }
  }

  @Override
  public Properties getProperties() {
    return properties;
  }

  @Override
  public void setProperties(Properties properties) {
    this.properties = properties;
  }
}
