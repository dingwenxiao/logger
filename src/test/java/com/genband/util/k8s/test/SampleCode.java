package com.genband.util.k8s.test;


import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.Filter.Result;
import org.apache.logging.log4j.core.appender.mom.kafka.KafkaAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.filter.CompositeFilter;
import org.apache.logging.log4j.core.filter.ThresholdFilter;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.genband.util.log.LogConfigurationUtil;
import com.genband.util.log.config.ConfigManager;
import com.genband.util.log.config.KafkaConfigManager;
import com.genband.util.log.constants.LogConfigConstants;
import com.genband.util.log.constants.Topics;

public class SampleCode {
  private static Logger logger = LoggerFactory.getLogger(SampleCode.class.getName());

  public static void main(String[] args) {
    // logger.info("I am test hello");
    // logger.debug("i am test debug");
    // logger.trace("I am test trace");
    // logger.warn("I am a test warn");
    // logger.error("I am a test error");
    String kafkaAddress1 = "192.168.56.100:9092";
    String[] appenderNames =
        {"kafka-info", "kafka-debug", "kafka-trace", "kafka-error", "kafka-warn"};

    String topicError = "error";
    String topicInfo = "info";
    String topicWarn = "warn";
    String topicDebug = "debug";
    String topicTrace = "trace";

    testConnection(kafkaAddress1);

//    try {
//      addAppender(kafkaAddress1, "kafka-info", topicInfo, Level.INFO, Level.WARN);
//      addAppender(kafkaAddress1, "kafka-debug", topicDebug, Level.DEBUG, Level.INFO);
//      addAppender(kafkaAddress1, "kafka-trace", topicTrace, Level.TRACE, Level.DEBUG);
//      addAppender(kafkaAddress1, "kafka-error", Topics.error.toString(), Level.ERROR, Level.FATAL);
//      addAppender(kafkaAddress1, "kafk-warn", Topics.warn.toString().toString(), Level.WARN,
//          Level.ERROR);
//
//      while (true) {
//        logger.info("I am test hello");
//        logger.debug("i am test debug");
//        logger.trace("I am test trace");
//        logger.warn("I am a test warn");
//        logger.error("I am a test error");
//        Thread.sleep(10 * 1000);
//      }
//    } catch (Exception e) {
//
//    }

    //
    // removeAppender(appenderNames);
    //
    // String kafkaAddress2 = "192.168.56.102:9092";
    // addAppender(kafkaAddress2, "kafka-info", topicInfo, Level.INFO, Level.WARN);
    // addAppender(kafkaAddress2, "kafka-debug", topicDebug, Level.DEBUG, Level.INFO);
    // addAppender(kafkaAddress2, "kafka-trace", topicTrace, Level.TRACE, Level.DEBUG);
    // addAppender(kafkaAddress2, "kafka-error", Topics.error.toString(), Level.ERROR, Level.FATAL);
    // addAppender(kafkaAddress2, "kafk-warn", Topics.warn.toString().toString(), Level.WARN,
    // Level.ERROR);
    //
    // logger.info("I am test hello");
    // logger.debug("i am test debug");
    // logger.trace("I am test trace");
    // logger.warn("I am a test warn");
    // logger.error("I am a test error");
  }

  public static void addAppender(String kafkaAddress, String appenderName, String topicName,
      Level thresholdFilterOneLevel, Level thresholdFilterTwoLevel) {
    final LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
    final Configuration configuration = loggerContext.getConfiguration();

    Property[] properties = new Property[] {
        Property.createProperty(LogConfigConstants.bootstrap_servers.toString(), kafkaAddress),
        Property.createProperty("retries", "0"),
        Property.createProperty("metadata.fetch.timeout.ms", "0")};

    Filter[] filter =
        new ThresholdFilter[] {ThresholdFilter.createFilter(thresholdFilterOneLevel, null, null),
            ThresholdFilter.createFilter(thresholdFilterTwoLevel, Result.DENY, Result.NEUTRAL)};

    CompositeFilter compositeFilter = CompositeFilter.createFilters(filter);

    PatternLayout patternLayout = PatternLayout.createLayout("%r [%t] %p %c %x - %m%n", null,
        configuration, null, null, false, false, null, null);

    KafkaAppender kafkaAppender = KafkaAppender.createAppender(patternLayout, compositeFilter,
        appenderName, true, topicName, properties);
    kafkaAppender.start();

    configuration.addAppender(kafkaAppender);

    LoggerConfig loggerConfig = configuration.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
    loggerConfig.addAppender(kafkaAppender, Level.ERROR, null);
  }

  public static void removeAppender(String[] appenderNames) {
    if (appenderNames != null && appenderNames.length != 0) {

      final LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
      final Configuration configuration = loggerContext.getConfiguration();
      LoggerConfig loggerConfig = configuration.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
      for (String appenderName : appenderNames) {
        loggerConfig.removeAppender(appenderName);
      }

    }
  }
  
  public static boolean testConnection(String kafkaAddress) {
      Properties props = new Properties();
      props.put("bootstrap.servers", kafkaAddress);
      props.put("acks", "all");
      props.put("retries", 1);
      props.put("batch.size", 16384);
      props.put("linger.ms", 1);
      props.put("buffer.memory", 33554432);
      props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
      props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
      props.put("request.timeout.ms", 10000);
      props.put("max.block.ms", 10000);
      props.put("metadata.fetch.timeout.ms", "2000");

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
