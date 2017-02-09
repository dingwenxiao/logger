package com.genband.util.log.config;

import java.net.URI;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Order;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.core.config.plugins.Plugin;

import com.genband.util.log.LogConfigurationUtil;
import com.genband.util.log.constants.LogConfigConstants;

@Plugin(name = "CustomConfigurationFactory", category = ConfigurationFactory.CATEGORY)
@Order(5)
public class CustomConfigurationFactory extends ConfigurationFactory {

  static Configuration createConfiguration(final String name,
      ConfigurationBuilder<BuiltConfiguration> builder) {

    ConfigManager configManager = KafkaConfigManager.getInstance();
    builder.setConfigurationName(name);
    builder.setStatusLevel(Level.INFO);
    builder.add(builder.newFilter("ThresholdFilter", Filter.Result.ACCEPT, Filter.Result.NEUTRAL)
        .addAttribute(LogConfigConstants.level.toString(), Level.ERROR));

    AppenderComponentBuilder appenderBuilder = builder.newAppender("Stdout", "CONSOLE")
        .addAttribute(LogConfigConstants.target.toString(), ConsoleAppender.Target.SYSTEM_OUT);
    appenderBuilder.add(builder.newLayout(LogConfigConstants.PatternLayout.toString())
        .addAttribute(LogConfigConstants.pattern.toString(), configManager.getPatternLayout()));
    appenderBuilder.add(builder.newFilter("MarkerFilter", Filter.Result.DENY, Filter.Result.NEUTRAL)
        .addAttribute("marker", "FLOW"));
    builder.add(appenderBuilder);
    builder.add(builder.newLogger("org.apache.logging.log4j", Level.ALL)
        .add(builder.newAppenderRef("Stdout")).addAttribute("additivity", false));
    builder.add(
        builder.newRootLogger(LogConfigurationUtil.getLogLevelConfig(configManager.getLogLevel()))
            .add(builder.newAppenderRef("Stdout")));
    return builder.build();
  }

  @Override
  public Configuration getConfiguration(final ConfigurationSource source) {
    return getConfiguration(source.toString(), null);
  }

  @Override
  public Configuration getConfiguration(final String name, final URI configLocation) {
    ConfigurationBuilder<BuiltConfiguration> builder = newConfigurationBuilder();
    return createConfiguration(name, builder);
  }

  @Override
  protected String[] getSupportedTypes() {
    return new String[] {"*"};
  }

}
