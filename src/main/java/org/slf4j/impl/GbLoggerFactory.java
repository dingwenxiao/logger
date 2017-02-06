package org.slf4j.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.simple.SimpleLogger;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import com.genband.util.log.LoggerOperation;

public class GbLoggerFactory implements ILoggerFactory {
  ConcurrentMap<String, Logger> loggerMap;
  boolean isAppenderInitialized = false;

  public GbLoggerFactory() {
    loggerMap = new ConcurrentHashMap<>();
    // force log4j to initialize
    org.apache.logging.log4j.LogManager.getRootLogger();
  }

  /**
   * Return an appropriate {@link SimpleLogger} instance by name.
   */
  public Logger getLogger(String name) {

    if (!isAppenderInitialized) {
      isAppenderInitialized = true;
      if (LoggerOperation.LoggerAppenderUpdate()) {
        LoggerOperation.startWatch();
      }
    }

    Logger gbLogger = loggerMap.get(name);
    if (gbLogger != null) {
      return gbLogger;
    } else {
      org.apache.logging.log4j.Logger log4jLogger;
      if (name.equalsIgnoreCase(Logger.ROOT_LOGGER_NAME))
        log4jLogger = LogManager.getRootLogger();
      else
        log4jLogger = LogManager.getLogger(name);

      Logger newInstance = new GbLoggerAdapter(log4jLogger);
      Logger oldInstance = loggerMap.putIfAbsent(name, newInstance);
      return oldInstance == null ? newInstance : oldInstance;
    }
  }

  void reset() {
    loggerMap.clear();
  }

}
