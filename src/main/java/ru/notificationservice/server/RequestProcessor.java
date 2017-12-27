package ru.notificationservice.server;

import org.apache.commons.validator.ValidatorException;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.notificationservice.notification.Notification;
import ru.notificationservice.server.notifier.HttpNotifier;
import ru.notificationservice.utils.HeaderName;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;



class RequestProcessor implements Runnable {

    private Logger log = LoggerFactory.getLogger(RequestProcessor.class);

    private Socket clientSocket;

    private Scheduler scheduler;

    RequestProcessor(Socket clientSocket, Scheduler scheduler) {
        this.clientSocket = clientSocket;
        this.scheduler = scheduler;
    }

    public void run() {
        try (ObjectInputStream input =
                     new ObjectInputStream(clientSocket.getInputStream())) {

            Notification notification = (Notification) input.readObject();

            JobDetail job = null;

            switch (notification.get(HeaderName.NOTIFICATION_TYPE)) {
                case "http":
                    job = createHttpJobDetail(notification);
                    break;
                case "mail":
                    job = createMailJobDetail(notification);
                    break;
                default:
                    throw new ValidatorException();
            }

            scheduler.scheduleJob(job, createTrigger(notification, job));

        } catch (ClassNotFoundException e) {
            log.error("Error can not get an InputStream");
        } catch (IOException e) {
            log.error("Error can not get an InputStream");
        } catch (ValidatorException e) {
            log.error("Error TCP parameters are not valid");
        } catch (SchedulerException e) {
            log.error("Error can not add {} to {}", JobDetail.class, scheduler.getClass());
        } finally {
            try {
                if(clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private JobDetail createHttpJobDetail(Notification notification) {
        return JobBuilder
                .newJob(HttpNotifier.class)
                .withIdentity(notification.get(HeaderName.EXTERNAL_ID), "Group 1")
                .usingJobData(HeaderName.MESSAGE.toString(), notification.get(HeaderName.MESSAGE))
                .usingJobData(HeaderName.URL.toString(), notification.get(HeaderName.URL))
                .build();
    }

    private JobDetail createMailJobDetail(Notification notification) {
        return JobBuilder
                .newJob(HttpNotifier.class)
                .withIdentity(notification.get(HeaderName.EXTERNAL_ID), "Group 1")
                .usingJobData(HeaderName.MESSAGE.toString(), notification.get(HeaderName.MESSAGE))
                .usingJobData(HeaderName.EMAIL.toString(), notification.get(HeaderName.EMAIL))
                .build();
    }

    private Trigger createTrigger(Notification notification, JobDetail job)
            throws DateTimeParseException {
        LocalTime time = LocalTime.parse(notification.get(HeaderName.TIME));

        return TriggerBuilder.newTrigger()
                .withIdentity(notification.get(HeaderName.EXTERNAL_ID))
                .startAt(DateBuilder.todayAt(time.getHour(), time.getMinute(), time.getSecond()))
                .forJob(job)
                .build();
    }
}
