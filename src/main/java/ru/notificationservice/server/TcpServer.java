package ru.notificationservice.server;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class TcpServer {

    private static final Logger log = LoggerFactory.getLogger(TcpServer.class);

    private static final int DEFAULT_PORT = 8080;

    private final SchedulerFactory schedulerFactory;

    private final Scheduler scheduler;

    private boolean isShutDown = Boolean.FALSE;

    private ServerSocket server;

    {
        try {

            schedulerFactory = new StdSchedulerFactory();
            scheduler = schedulerFactory.getScheduler();
            scheduler.start();

        } catch (SchedulerException e) {
            log.error("Error can not get {}", Scheduler.class);
            throw new RuntimeException(e);
        }
    }


    public TcpServer() {
    }

    public void start() {

        ExecutorService executorService = Executors.newFixedThreadPool(5);

        try(ServerSocket serverSocket = new ServerSocket(DEFAULT_PORT)) {

            server = serverSocket;

            while (!isShutDown) {

                try {

                    //Process the request in a new thread
                    /*executorService.execute(
                            new RequestProcessor(
                                    serverSocket.accept(),
                                    scheduler));*/

                    executorService.execute(
                            new RequestProcessorStub(serverSocket.accept()));

                } catch (IOException e) {
                    throw new RuntimeException(
                            "Error accepting client connection", e);
                }
            }

        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            log.info("{} {}",
                    getClass().getName(),
                    getClass().getEnclosingMethod().getName());
        }
    }

    public static void main(String[] args) {
        new TcpServer().start();
    }

    public void shutDown() {

        log.info("{} {}",
                getClass().getName(),
                getClass().getEnclosingMethod().getName());

        isShutDown = Boolean.TRUE;

        try {
            if (server != null) {
                server.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isShutDown() {
        return isShutDown;
    }
}
