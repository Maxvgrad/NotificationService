package ru.notificationservice.utils;

import org.apache.commons.validator.ValidatorException;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Максим on 08.12.2017.
 */
public class HeaderValidator {

    private static final Logger log = LoggerFactory.getLogger(HeaderValidator.class);

    public static String validateMail(String email)
            throws ValidatorException {

        EmailValidator validator = EmailValidator.getInstance();

        if (!validator.isValid(email)) {
            throw new ValidatorException("Invalid email");
        }

        return email;
    }

    public static String validateUrl(String url)
            throws ValidatorException {

        UrlValidator validator = new UrlValidator();

        if (!validator.isValid(url)) {
            throw new ValidatorException("Invalid URL");
        }

        return url;
    }

    public static String validateStr(String str)
            throws ValidatorException{

        if (str == null || "".equals(str)) {
            throw new ValidatorException("String is empty");
        }

        return str;
    }

    public static String validateNotificationType(String notificationType)
            throws ValidatorException {
        switch (notificationType) {
            case "http":
            case "mail":
                return notificationType;
            default:
                throw new ValidatorException("Not valid notification type");
        }
    }
}
