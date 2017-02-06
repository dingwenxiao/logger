package com.genband.util.log;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Filter.Result;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.mom.kafka.KafkaAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.filter.CompositeFilter;
import org.apache.logging.log4j.core.filter.ThresholdFilter;
import org.apache.logging.log4j.core.layout.PatternLayout;

/**
 * Dynamically add kafka log4j appender
 * 
 * @author dixiao
 *
 */
public class LogConfigurationUtil {

  /**
   * 
   * @param configManager configuration class
   * @param kafkaAddress kafka address
   * @param appenderName logger appender name
   * @param topicName topic name
   * @param thresholdFilterOneLevel
   * @param thresholdFilterTwoLevel
   */
  public static void addKafkaAppender(ConfigManager configManager, String kafkaAddress,
      String appenderName, String topicName, Level thresholdFilterOneLevel,
      Level thresholdFilterTwoLevel) {
    final LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
    final Configuration configuration = loggerContext.getConfiguration();

    Property[] properties = new Property[] {
        Property.createProperty(LogConfigConstants.bootstrap_servers.toString(), kafkaAddress)};

    Filter[] filter =
        new ThresholdFilter[] {ThresholdFilter.createFilter(thresholdFilterOneLevel, null, null),
            ThresholdFilter.createFilter(thresholdFilterTwoLevel, Result.DENY, Result.NEUTRAL)};

    CompositeFilter compositeFilter = CompositeFilter.createFilters(filter);

    PatternLayout patternLayout = PatternLayout.createLayout(
        configManager.getProperties().getProperty(LogConfigConstants.pattern_layout.toString()), null,
        configuration, null, null, false, false, null, null);

    KafkaAppender kafkaAppender = KafkaAppender.createAppender(patternLayout, compositeFilter,
        appenderName, true, topicName, properties);
    kafkaAppender.start();

    configuration.addAppender(kafkaAppender);

    LoggerConfig loggerConfig = configuration.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
    loggerConfig.addAppender(kafkaAppender, getLogLevelConfig(
        configManager.getProperties().getProperty(LogConfigConstants.log_level.toString())), null);
    loggerContext.updateLoggers(configuration);
  }

  private static Level getLogLevelConfig(String level) {
    if (level != null)
      switch (level) {
        case "info":
          return Level.INFO;
        case "error":
          return Level.ERROR;
        case "warn":
          return Level.WARN;
        case "fatal":
          return Level.FATAL;
        case "debug":
          return Level.DEBUG;
        case "trace":
          return Level.TRACE;
        case "all":
          return Level.ALL;
      }
    return Level.OFF;
  }

}
