package com.genband.util.log;

import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.genband.util.log.config.ConfigManager;
import com.genband.util.log.config.KafkaConfigManager;
import com.genband.util.log.constants.LogConfigConstants;
import com.genband.util.log.constants.Topics;

import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;

public class LoggerOperation {

  private static final Logger logger = LogManager.getLogger(LoggerOperation.class);
  private static ConfigManager configManager = null;

  public static boolean LoggerAppenderUpdate() {
    configManager = new KafkaConfigManager(LogConfigConstants.default_log_path.toString());
    new KubernetesNetworkService.SingletonBuilder(configManager).build();
    KubernetesNetworkService kubernetesNetworkService = KubernetesNetworkService.getInstance();
    String kafkaAddress = getKakfaAddress(kubernetesNetworkService);

    if (kafkaAddress == null || "".equals(kafkaAddress)) {
      return false;
    }

    try {
      addAppender(configManager, kafkaAddress);
    } catch (Exception e) {
      logger.error(e);
      logger.error("failed to connect kafka");
      return false;
    }

    return true;
  }

  private static void addAppender(ConfigManager configManager, String kafkaAddress) {
    String[] topics = configManager.getTopics();
    if (topics != null) {
      for (String topic : topics) {
        if (topic.equals(Topics.info.toString())) {
          LogConfigurationUtil
              .addKafkaAppender(configManager, kafkaAddress,
                  configManager.getProperties().getProperty(LogConfigConstants.service.toString())
                      + "-" + Topics.info.toString(),
                  Topics.info.toString(), Level.INFO, Level.WARN);
        } else if (topic.equals(Topics.error.toString())) {
          LogConfigurationUtil
              .addKafkaAppender(configManager, kafkaAddress,
                  configManager.getProperties().getProperty(LogConfigConstants.service.toString())
                      + "-" + Topics.debug.toString(),
                  Topics.debug.toString(), Level.DEBUG, Level.INFO);
        } else if (topic.equals(Topics.error.toString())) {
          LogConfigurationUtil
              .addKafkaAppender(configManager, kafkaAddress,
                  configManager.getProperties().getProperty(LogConfigConstants.service.toString())
                      + "-" + Topics.trace.toString(),
                  Topics.trace.toString(), Level.TRACE, Level.DEBUG);
        } else if (topic.equals(Topics.error.toString())) {
          LogConfigurationUtil
              .addKafkaAppender(configManager, kafkaAddress,
                  configManager.getProperties().getProperty(LogConfigConstants.service.toString())
                      + "-" + Topics.error.toString(),
                  Topics.error.toString(), Level.ERROR, Level.FATAL);
        } else {
          LogConfigurationUtil.addKafkaAppender(configManager, kafkaAddress,
              configManager.getProperties().getProperty(LogConfigConstants.service.toString()) + "-"
                  + Topics.warn.toString(),
              Topics.warn.toString().toString(), Level.WARN, Level.ERROR);
        }
      }
    }
  }

  private static String getKakfaAddress(KubernetesNetworkService kubernetesNetworkService) {
    String address = null;

    if (testKafkaConnection(configManager,
        configManager.getProperties().getProperty(LogConfigConstants.kafka_dns.toString()))) {
      address = configManager.getProperties().getProperty(LogConfigConstants.kafka_dns.toString());
      // "172.28.247.239:9092"
    } else {
      List<String> fetchKafkaAddress = kubernetesNetworkService.getEndPointsAddressFromConfigMap();

      if (fetchKafkaAddress != null && !fetchKafkaAddress.isEmpty()) {
        logger.info(fetchKafkaAddress.toString());
        address = fetchKafkaAddress.get(0);
      } else {
        logger.error("Kafka adderss grabbed from kubernetes is empty");
      }
    }
    // the kafka address is not ready yet.
    return address;
  }

  public static void startWatch() {
    // new Thread(new KubernetesMonitorThread(KubernetesNetworkService.getInstance())).start();
    try (Watch watch = KubernetesNetworkService.getInstance().getEndPointsWatcher()) {
      logger.info("Watching logs");
      // closeLatch.await(10, TimeUnit.SECONDS);
    } catch (KubernetesClientException e) {
      logger.error("Could not watch resources", e);
    }
    try {
      Thread.sleep(10);
    } catch (InterruptedException e) {
      logger.error(e);
    } catch (Exception e) {
      logger.error(e);
    }
  }

  private static boolean testKafkaConnection(ConfigManager configManager, String kafkaAddress) {
    try {
      LogConfigurationUtil.addKafkaAppender(configManager, kafkaAddress,
          configManager.getProperties().getProperty(LogConfigConstants.service.toString()) + "-"
              + Topics.info.toString(),
          Topics.info.toString(), Level.INFO, Level.WARN);
    } catch (Exception e) {
      logger.error("failed to connect to kafka", e);
      return false;
    }
    return true;
  }

}
