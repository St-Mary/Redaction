package com.stmarygate.redaction;

import com.stmarygate.redaction.bot.StMaryRedactor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
  public static void main(String[] args) {
    Logger LOGGER = LoggerFactory.getLogger(Main.class);
    try {
      new StMaryRedactor();
    } catch (Exception e) {
      LOGGER.error("Error while starting bot: ", e);
      System.exit(1);
    }
  }
}
