package ru.notificationservice.utils;

public enum HeaderName {
    EXTERNAL_ID("external_id"), MESSAGE("message"), TIME("time"),
    NOTIFICATION_TYPE("notification_type"), EMAIL("email"), URL("url");

    private String msg;

    HeaderName(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return msg;
    }


}
