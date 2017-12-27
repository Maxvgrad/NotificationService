package ru.notificationservice.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.notificationservice.notification.Notification;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Objects;


public class TcpClient {

    private static final Logger log = LoggerFactory.getLogger(TcpClient.class);

    private Socket socket;

    private Notification notification;

    private TcpClient() {
    }

    public static TcpClient getInstance(Notification notification) {
        TcpClient client = new TcpClient();
        client.notification = Objects.requireNonNull(notification);
        return client;
    }

    public void addNotification() {

        try {

            socket = new Socket("localhost", 8080);

            try(ObjectOutputStream output =
                        new ObjectOutputStream(socket.getOutputStream())) {

                output.writeObject(notification);

                output.flush();

            } catch (IOException e) {
                e.printStackTrace();
                log.error("{}", e.getMessage());
                throw new RuntimeException(e.getCause());
            }

        } catch (IOException e) {
            log.error("I/O error occurs when creating the socket");
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                    log.debug("Closing socket");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
