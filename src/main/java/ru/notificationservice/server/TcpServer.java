package ru.notificationservice.server;

import org.apache.commons.validator.ValidatorException;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.notificationservice.utils.NotificationProtocol;
import ru.notificationservice.utils.NotificationType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ru.notificationservice.utils.NotificationProtocol.*;


public class TcpServer {

    private static final Logger log = LoggerFactory.getLogger(TcpServer.class);

    private static final int DEFAULT_PORT = 8080;

    private ServerSocket serverSocket;

    private SchedulerFactory schedulerFactory;

    private Scheduler scheduler;

    private boolean isShutDown = Boolean.FALSE;

    public TcpServer(int port) {
        try {

            serverSocket = new ServerSocket(port);
            schedulerFactory = new StdSchedulerFactory();
            scheduler = schedulerFactory.getScheduler();

        } catch (IOException e) {
            log.error("Error opening a Server Socket on port {}", port);
            throw new RuntimeException(e);
        } catch (SchedulerException e) {
            log.error("Error can not get {}", Scheduler.class);
            throw new RuntimeException(e);
        }
    }

    public void start() {

        try {
            scheduler.start();
        } catch (SchedulerException e) {
            log.error("Error can not get {}", Scheduler.class);
            throw new RuntimeException(e);
        }

        ExecutorService executorService = Executors.newFixedThreadPool(5);

        while(!isShutDown){
            Socket clientSocket = null;
            log.info("Waiting a connection");
            try {
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                throw new RuntimeException(
                        "Error accepting client connection", e);
            }
            log.info("Client {} connected", clientSocket.getPort());

            executorService.execute(new RequestProcessor(clientSocket));
        }
    }

    public static void main(String[] args) {
        new TcpServer(DEFAULT_PORT).start();
    }


    private class RequestProcessor implements Runnable {

        private Socket clientSocket;

        private Scheduler scheduler;

        RequestProcessor(Socket clientSocket) {
            this.clientSocket = clientSocket;
            this.scheduler = TcpServer.this.scheduler;
        }

        public void run() {
            log.info("The thread {} starts processing the request", Thread.currentThread().getName());
            Map<String, String> headersMap = new HashMap<>();

            try {
                InputStream inputStream = clientSocket.getInputStream();

                BufferedReader bufferedReader =
                        new BufferedReader(new InputStreamReader(inputStream));
                log.debug("Headers reading:");
                while (bufferedReader.ready()) {
                    String[] nameValueArr = bufferedReader.readLine().split("=", 2);
                    headersMap.put(nameValueArr[0], nameValueArr[1]);
                    log.debug("{} = {}", nameValueArr[0], nameValueArr[1]);
                }
                NotificationProtocol protocol = new NotificationProtocol();

                Map validHeaderMap = protocol.validateAndConvertHeaders(headersMap);

                LocalTime time = (LocalTime)validHeaderMap.get(TIME);
                String extraParam = protocol.getExtraParamName(
                        (NotificationType) validHeaderMap.get(NOTIFICATION_TYPE));

                JobDetail job = JobBuilder
                        .newJob((Class<? extends Job>) validHeaderMap.get(NOTIFIER))
                        .withIdentity((String) validHeaderMap.get(EXTERNAL_ID), "Group 1")
                        .usingJobData(MESSAGE, (String) validHeaderMap.get(MESSAGE))
                        .usingJobData(extraParam, (String) validHeaderMap.get(extraParam))
                        .build();

                Trigger trigger = TriggerBuilder.newTrigger()
                        .withIdentity((String) validHeaderMap.get(EXTERNAL_ID))
                        .startAt(DateBuilder.todayAt(time.getHour(), time.getMinute(), time.getSecond()))
                        .forJob(job)
                        .build();


                log.info("Notification #{}, trigger start time: {}"
                        , validHeaderMap.get(EXTERNAL_ID)
                        , trigger.getStartTime());

                scheduler.scheduleJob(job, trigger);

            } catch (IOException e) {
                log.error("Error can not get an InputStream");
            } catch (ValidatorException e) {
                log.error("Error TCP parameters are not valid");
            } catch (SchedulerException e) {
                log.error("Error can not add {} to {}", JobDetail.class, scheduler.getClass());
            } finally {
                try {
                    log.debug("closing client socket");
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void shutDown() {
        isShutDown = Boolean.TRUE;
    }
}
