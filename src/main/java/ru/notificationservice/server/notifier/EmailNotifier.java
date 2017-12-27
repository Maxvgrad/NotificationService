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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Properties;


public class EmailNotifier implements Job {

    private String message;

    private String mailAddress;

    private static final Logger log = LoggerFactory.getLogger(EmailNotifier.class);

    public EmailNotifier() {
    }

    //Constructor for JUnit test
    public EmailNotifier(String mailAddress, String message) {
        this.message = message;
        this.mailAddress = mailAddress;
    }

    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap map = jobExecutionContext != null ?
                jobExecutionContext.getMergedJobDataMap() : null;

        log.info("{} starts at {}, with message: {}", getClass(), LocalDateTime.now(), message);

        File file;

        try {

            file = new File(getClass()
                                    .getClassLoader()
                                    .getResource("email.properties")
                                    .getFile());

        } catch (NullPointerException e) {
            throw new RuntimeException("Properties file is not found");
        }

        try (FileReader reader = new FileReader(file)){

            Properties prop = new Properties();
            prop.load(reader);

            Email email = new SimpleEmail();

            email.setHostName(prop.getProperty("host_name"));
            log.info("host_name={}", prop.getProperty("host_name"));


            email.setSmtpPort(Integer.parseInt(prop.getProperty("stmp_port")));
            log.info("stmp_port={}", prop.getProperty("stmp_port"));

            email.setAuthenticator(new DefaultAuthenticator(
                    prop.getProperty("user_name"),
                    prop.getProperty("user_password")));

            email.setSSLOnConnect(true);

            email.setFrom(prop.getProperty("email_from"));
            log.info("email_from={}", prop.getProperty("email_from"));

            email.setSubject(prop.getProperty("subject"));
            log.info("subject={}", prop.getProperty("subject"));

            email.setMsg(message);

            email.addTo(mailAddress);

            email.send();

        } catch (IOException e) {
            log.error("IOException");
        } catch (EmailException e) {
            log.error("Error sending email");
            throw new JobExecutionException(e.getMessage());
        }

        log.info("Message sent");

    }
}
