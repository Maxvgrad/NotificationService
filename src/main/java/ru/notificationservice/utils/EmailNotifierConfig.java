package ru.notificationservice.utils;

/**
 * Created by Максим on 09.12.2017.
 */
public final class EmailNotifierConfig {
    private EmailNotifierConfig() {}

    public static final String HOST_NAME = "smtp.gmail.com";
    public static final int SMTP_PORT = 465;
    public static final String USER_NAME = "user";
    public static final String USER_PASSWORD = "password";
    public static final String EMAIL_FROM = "test@gmail.com";

}
