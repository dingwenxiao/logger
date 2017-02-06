package com.genband.util.k8s.test;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.genband.util.log.KafkaConfigManager;
import com.genband.util.log.KubernetesNetworkService;
import com.genband.util.log.LogConfigConstants;

/**
 * This file should be removed after Dingwen checked.
 * 
 * @author hakuang
 *
 */
public class SampleCode {
  private static Logger logger = LoggerFactory.getLogger(SampleCode.class.getName());

  public static void main(String[] args) {
    logger.info("hello");

    // ConfigManager configManager = new KafkaConfigManager(LogConfig.default_log_path.toString());
    // /**
    // * move these into test cases which can be used as example as well.
    // */
    // LogConfigurationUtil.addKafkaAppender(configManager, getKakfaAddress(), "kafka-info", "info",
    // Level.INFO, Level.WARN);
    // LogConfigurationUtil.addKafkaAppender(configManager, getKakfaAddress(), "kafka-debug",
    // "debug",
    // Level.DEBUG, Level.INFO);
    // LogConfigurationUtil.addKafkaAppender(configManager, getKakfaAddress(), "kafka-trace",
    // "trace",
    // Level.TRACE, Level.DEBUG);
    // LogConfigurationUtil.addKafkaAppender(configManager, getKakfaAddress(), "kafka-error",
    // "error",
    // Level.ERROR, Level.FATAL);
    // LogConfigurationUtil.addKafkaAppender(configManager, getKakfaAddress(), "kafka-warn", "warn",
    // Level.WARN, Level.ERROR);
    //
    // logger.info("I am test hello");
    // logger.debug("i am test debug");
    // logger.trace("I am test trace");
    // logger.warn("I am a test warn");
    // logger.error("I am a test error");
  }

  private static String getKakfaAddress() {

    new KubernetesNetworkService.SingletonBuilder(
        new KafkaConfigManager(LogConfigConstants.default_log_path.toString())).build();
    KubernetesNetworkService kubernetesNetworkService = KubernetesNetworkService.getInstance();
    
    List<String> fetchKafkaAddress = kubernetesNetworkService.getEndPointsAddressFromConfigMap();

    if (fetchKafkaAddress != null) {
      logger.info(fetchKafkaAddress.toString());
    } else {
      logger.info("Kafka adderss is empty");
    }

    // the kafka address is not ready yet.
    return "172.28.247.239:9092";
  }
}