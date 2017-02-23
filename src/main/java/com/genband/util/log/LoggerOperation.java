package com.genband.util.log;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
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
    configManager = KafkaConfigManager.getInstance();
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
        } else if (topic.equals(Topics.debug.toString())) {
          LogConfigurationUtil
              .addKafkaAppender(configManager, kafkaAddress,
                  configManager.getProperties().getProperty(LogConfigConstants.service.toString())
                      + "-" + Topics.debug.toString(),
                  Topics.debug.toString(), Level.DEBUG, Level.INFO);
        } else if (topic.equals(Topics.trace.toString())) {
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
    } else {
      List<String> fetchKafkaAddress = kubernetesNetworkService.getEndPointsAddressFromConfigMap();

      if (fetchKafkaAddress != null && !fetchKafkaAddress.isEmpty()) {
        logger.info(fetchKafkaAddress.toString());
        address = fetchKafkaAddress.get(0);
      } else {
        logger.error("Kafka adderss grabbed from kubernetes is empty");
      }
    }
    //192.168.33.10:9092
    return address;
  }

  private static boolean testKafkaConnection(ConfigManager configManager, String kafkaAddress) {
    Properties props = new Properties();
    props.put("bootstrap.servers", kafkaAddress);
    props.put("acks", "all");
    props.put("retries", 0);
    props.put("batch.size", 16384);
    props.put("linger.ms", 1);
    props.put("buffer.memory", 33554432);
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    props.put("request.timeout.ms", 10000);
    props.put("max.block.ms", 10000);
    props.put("metadata.fetch.timeout.ms", 5000);

    Producer<String, String> producer = new KafkaProducer<>(props);
    try {
      producer.send(new ProducerRecord<String, String>("test-topic", "test", "test")).get();
      producer.close();
    } catch (Exception e) {
      logger.error("failed to connect to kafka", e);
      return false;
    }
    return true;
  }

}
