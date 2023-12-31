package com.stmarygate.redaction;

import io.github.cdimascio.dotenv.Dotenv;

public class Constants {
  public static final String REGION_EMOJI = "\uD83C\uDF0D";
  public static final String DESCRIPTION_EMOJI = "\uD83C\uDF67";
  public static final String VILLAGE_EMOJI = "\uD83C\uDFD8️";
  public static final String PLACE_EMOJI = "\uD83D\uDCCD";
  private static final Dotenv dotenv;

  static {
    dotenv = Dotenv.configure().ignoreIfMalformed().ignoreIfMissing().load();
  }

  public static String getToken() {
    return dotenv.get("token");
  }

  public static String getLogChannel() {
    return dotenv.get("log_channel");
  }

  public static String getDatabaseHost() {
    return dotenv.get("database_host");
  }

  public static String getDatabasePort() {
    return (dotenv.get("database_port") == null || dotenv.get("database_port").isEmpty())
        ? ""
        : ":" + dotenv.get("database_port");
  }

  public static String getDatabaseName() {
    return dotenv.get("database_name");
  }

  public static String getDatabaseUsername() {
    return dotenv.get("database_username");
  }

  public static String getDatabasePassword() {
    return dotenv.get("database_password");
  }
}
