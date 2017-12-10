package ru.notificationservice.server.notifier;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

import static ru.notificationservice.utils.EmailNotifierConfig.*;
import static ru.notificationservice.utils.NotificationProtocol.EMAIL;
import static ru.notificationservice.utils.NotificationProtocol.MESSAGE;


public class EmailNotifier implements Job {

    String message;

    String mailAddress;

    private static final Logger log = LoggerFactory.getLogger(EmailNotifier.class);

    public EmailNotifier() {
    }

    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap map = jobExecutionContext.getMergedJobDataMap();

        mailAddress = map.getString(EMAIL);
        message = map.getString(MESSAGE);

        log.info("{} starts at {}, with message: {}", getClass(), LocalDateTime.now(), message);

        try {
            Email email = new SimpleEmail();
            email.setHostName(HOST_NAME);
            email.setSmtpPort(SMTP_PORT);
            email.setAuthenticator(new DefaultAuthenticator(USER_NAME, USER_PASSWORD));
            email.setSSLOnConnect(true);
            email.setFrom(EMAIL_FROM);
            email.setSubject("TestMail");
            email.setMsg(message);
            email.addTo(mailAddress);
            email.send();
        } catch (EmailException e) {
            log.error("Error sending email");
            throw new JobExecutionException(e.getMessage());
        } finally {

        }

        log.info("Message sent");

    }
}
