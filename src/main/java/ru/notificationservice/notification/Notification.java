package ru.notificationservice.notification;

import org.apache.commons.validator.ValidatorException;
import ru.notificationservice.utils.HeaderName;
import ru.notificationservice.utils.HeaderValidator;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.*;

public class Notification implements Serializable {

    private Map<HeaderName, String> headers = new HashMap<>();

    private String notification;

    private Notification() {}

    public static Notification getInstance(String message,
                                           LocalTime time,
                                           String notificationType,
                                           String extraParameter) {

        Notification notification = new Notification();

        try {
            notification.addHeader(
                    HeaderName.EXTERNAL_ID,
                    UUID.randomUUID().toString());

            notification.headers.put(
                    HeaderName.MESSAGE,
                    HeaderValidator.validateStr(message));

            notification.headers.put(
                    HeaderName.TIME,
                    Objects.requireNonNull(time).toString());

            switch(HeaderValidator.validateNotificationType(notificationType)) {
                case "http":
                    notification.headers.put(
                            HeaderName.URL,
                            HeaderValidator.validateUrl(extraParameter));
                    break;
                case "mail":
                    notification.headers.put(
                            HeaderName.EMAIL,
                            HeaderValidator.validateMail(extraParameter));
            }

            notification.headers.put(HeaderName.NOTIFICATION_TYPE, notificationType);

            return notification;

        } catch (ValidatorException | NullPointerException e) {
            throw new RuntimeException(e);
        }
    }

    private void addHeader(HeaderName name, String value) {
        headers.put(name, value);
    }

    private String removeHeader(HeaderName name) {
        return headers.remove(name);
    }

    public String get(HeaderName name) {
        return headers.get(name);
    }

    public boolean containHeader(HeaderName name) {
        return headers.containsKey(name);
    }

    public Set<HeaderName> nameSet() {
        return headers.keySet();
    }

    public Collection<String> values() {
        return headers.values();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification that = (Notification) o;
        return Objects.equals(headers, that.headers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(headers);
    }

    @Override
    public String toString() {
        if(notification != null) {
            return notification;
        } else {
            StringBuilder stringBuilder = new StringBuilder();

            for(Map.Entry<HeaderName, String> header : headers.entrySet()) {
                stringBuilder.append(String.format("%s: %s\r\n",
                        header.getKey(),
                        header.getValue()));
            }

            return notification = stringBuilder.toString();
        }
    }
}
