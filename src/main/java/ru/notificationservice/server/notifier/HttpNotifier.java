package ru.notificationservice.server.notifier;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.DataOutputStream;
import java.net.URL;
import java.time.LocalDateTime;


public class HttpNotifier implements Job {

    private static final Logger log = LoggerFactory.getLogger(HttpNotifier.class);

    private String message;

    private String targetURL;

    public HttpNotifier() {
    }

    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
//        JobDataMap map = jobExecutionContext.getMergedJobDataMap();

//        message = map.getString(MESSAGE);
//        targetURL = map.getString(URL);

        log.info("{} starts at {}, with message: {}", getClass(), LocalDateTime.now(), message);

        HttpsURLConnection connection = null;

        try {
            //Create connection
            URL url = new URL(targetURL);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "text/html; charset=utf-8");
//
            connection.setRequestProperty("Content-Length",
                    Integer.toString(message.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");
//
            connection.setUseCaches(false);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream (
                    connection.getOutputStream());
            wr.writeBytes(message);
            wr.flush();
            wr.close();

//            InputStream is = connection.getInputStream();
//            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
//            StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
//            String line;
//            while ((line = rd.readLine()) != null) {
//                log.info("Line: {}", line);
//                response.append(line);
//                response.append('\r');
//            }
//            rd.close();
//            return response.toString();
//            log.info("response: {}", response.toString());
        } catch (Exception e) {
            e.printStackTrace();
            throw new JobExecutionException("Error can not send message via HTTP");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

    }
}
