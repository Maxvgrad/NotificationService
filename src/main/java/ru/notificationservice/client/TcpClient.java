package ru.notificationservice.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.notificationservice.utils.NotificationType;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.time.LocalTime;
import java.util.UUID;

import static ru.notificationservice.utils.NotificationProtocol.*;


public class TcpClient {

    private static final Logger log = LoggerFactory.getLogger(TcpClient.class);

    private Socket socket;
    private OutputStream output;

    private String id;
    private String message;
    private String time;
    private NotificationType notificationType;
    private String extraParam;

    public TcpClient(NotificationType notificationType, String message, String time, String extraParam) {
        id = UUID.randomUUID().toString();
        this.message = message;
        this.time = time;
        this.notificationType = notificationType;
        this.extraParam = extraParam;
        log.debug("{} setup is done");
    }

    public static void main(String[] args) {
        LocalTime time = LocalTime.now().plusSeconds(15);
        new TcpClient(NotificationType.MAIL
                , "test msg1"
                , time.toString(), "mrbamma@yandex.ru").addNotification();

//        new TcpClient(NotificationType.HTTP
//                , "TEST MSG"
//                , time.toString()
//                , "https://requestb.in/17409i51?inspect").addNotification();
    }

    public void addNotification() {
        try {
            socket = new Socket("localhost", 8080);

            log.debug("Socket {}", socket);
            try {
                output = socket.getOutputStream();

                output.write(String.format("%s=%s\r\n", EXTERNAL_ID, id).getBytes());
                output.write(String.format("%s=%s\r\n", NOTIFICATION_TYPE, notificationType).getBytes());
                output.write(String.format("%s=%s\r\n", MESSAGE, message).getBytes());
                output.write(String.format("%s=%s\r\n", TIME, time).getBytes());
                log.debug("before switch");
                switch (notificationType) {
                    case HTTP:
                        output.write(String.format("%s=%s\r\n", URL, extraParam).getBytes());
                        log.debug("HTTP case");
                        break;
                    case MAIL:
                        log.debug("MAIL case");
                        output.write(String.format("%s=%s\r\n", EMAIL, extraParam).getBytes());
                }
                output.flush();

            } catch (IOException e) {
                log.error("I/O error write");
                throw new RuntimeException(e.getMessage());
            } finally {
                if (output != null) {
                    output.close();
                    log.debug("Closing output");
                }
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
