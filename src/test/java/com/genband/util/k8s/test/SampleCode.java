package com.genband.util.k8s.test;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleCode {
  private static Logger logger = LoggerFactory.getLogger(SampleCode.class.getName());

  public static void main(String[] args) {
     logger.info("I am test hello");
     logger.debug("i am test debug");
     logger.trace("I am test trace");
     logger.warn("I am a test warn");
     logger.error("I am a test error");
  }
}
