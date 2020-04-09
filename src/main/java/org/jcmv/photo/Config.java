package org.jcmv.photo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
  private static final Config instance = new Config();

  private Properties properties;

  public static Config getInstance() {
    return instance;
  }

  protected Config() {
    try (InputStream input = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
      if (input == null) {
        throw new NullPointerException("Unable to find config.properties");
      } else {
        properties = new Properties();
        properties.load(input);
      }
    } catch (IOException e) {
      throw new IllegalArgumentException("Unable to read config.properties", e);
    }
  }

  public String getProperty(String name) {
    return properties.getProperty(name);
  }
}
