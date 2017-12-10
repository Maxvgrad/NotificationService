package ru.notificationservice.utils;

/**
 * Created by Максим on 09.12.2017.
 */
public enum NotificationType {
    HTTP("http"), MAIL("mail");

    private String msg;

    NotificationType(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return msg;
    }
}
