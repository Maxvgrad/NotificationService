package ru.notificationservice.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

class RequestProcessorStub implements Runnable{

    private Logger log = LoggerFactory.getLogger(RequestProcessor.class);

    Socket clientSocket;

    public RequestProcessorStub(Socket socket) {
        this.clientSocket = socket;

    }

    @Override
    public void run() {

        try (ObjectInputStream inputStream =
                     new ObjectInputStream(clientSocket.getInputStream())) {
            Object obj = inputStream.readObject();
            log.info(obj.toString());

        } catch (ClassNotFoundException e){

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if(clientSocket != null) {
                try {
                    clientSocket.close();
                } catch (IOException e) {

                }
            }
        }
    }
}

