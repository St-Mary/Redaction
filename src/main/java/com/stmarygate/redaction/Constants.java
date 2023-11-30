package com.stmarygate.redaction;

import io.github.cdimascio.dotenv.Dotenv;

public class Constants {
    private static final Dotenv dotenv = Dotenv.configure()
            .ignoreIfMalformed()
            .ignoreIfMissing()
            .load();

    static {
        dotenv.load();
    }

    public static String getToken() {
        return dotenv.get("token");
    }

    public static String getLogChannel() {
        return dotenv.get("log_channel");
    }
}
