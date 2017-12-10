package ru.notificationservice.utils;

import org.apache.commons.validator.ValidatorException;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.notificationservice.server.notifier.EmailNotifier;
import ru.notificationservice.server.notifier.HttpNotifier;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.notificationservice.utils.NotificationType.HTTP;
import static ru.notificationservice.utils.NotificationType.MAIL;

/**
 * Created by Максим on 08.12.2017.
 */
public class NotificationProtocol {

    private static final Logger log = LoggerFactory.getLogger(NotificationProtocol.class);

    public static final String EXTERNAL_ID = "external_id";
    public static final String MESSAGE = "message";
    public static final String TIME = "time";
    public static final String NOTIFICATION_TYPE = "norification_type";
    public static final String EMAIL = "email";
    public static final String URL = "url";
    public static final String NOTIFIER = "notifier";

    private static final List<String> paramList = Arrays.asList("external_id",
            "message", "time", "norification_type");

    public Map validateAndConvertHeaders(Map<String, String> headersMap)
            throws ValidatorException {

        log.debug("Parameter map (size = {}) validation", headersMap.size());

        Map result = new HashMap();

        checkHeaders(headersMap);

        log.debug("Are you breaking on switch statement?");

        switch (NotificationType.valueOf(headersMap.get(NOTIFICATION_TYPE).toUpperCase())) {
            case HTTP:
                result.put(NOTIFIER, HttpNotifier.class);
                result.put(NOTIFICATION_TYPE, HTTP);
                result.put(URL, validateUrl(headersMap.get(URL)));
                break;
            case MAIL:
                result.put(NOTIFIER, EmailNotifier.class);
                result.put(NOTIFICATION_TYPE, MAIL);
                result.put(EMAIL, validateMail(headersMap.get(EMAIL)));
                break;
            default:
                throw new ValidatorException("Error not complete set of parameters");
        }

        log.debug("Fuuh, we pass the switch, don't worry");

        result.put(EXTERNAL_ID, validateStr(headersMap.get(EXTERNAL_ID)));
        result.put(MESSAGE, validateStr(headersMap.get(MESSAGE)));
        result.put(TIME, LocalTime.parse(headersMap.get(TIME)));

        return result;
    }

    private String validateMail(String email)
            throws ValidatorException {
        EmailValidator validator = EmailValidator.getInstance();
        if (!validator.isValid(email)) {
            throw new ValidatorException("Invalid email");
        }
        return email;
    }

    private String validateUrl(String url)
            throws ValidatorException {

//        java.net.URL result = null;

        UrlValidator validator = new UrlValidator();

        if (!validator.isValid(url)) {
            throw new ValidatorException("Invalid URL");
        }
        return url;
    }

    private String validateStr(String str)
            throws ValidatorException{
        if (str == null || "".equals(str)) {
            throw new ValidatorException("String is empty");
        }
        return str;
    }

    private boolean checkHeaders(Map<String, String> paramMap)
            throws ValidatorException{
        for(String param : paramList) {
            if (!paramMap.keySet().contains(param)) {
                throw new ValidatorException("Error not complete set of headers");
            }
        }
        return Boolean.TRUE;
    }

    public String getExtraParamName(NotificationType type) {
        switch (type) {
            case MAIL:
                return EMAIL;
            case HTTP:
                return URL;
            default:
                throw new RuntimeException("Not valid notification type");
        }
    }
}
