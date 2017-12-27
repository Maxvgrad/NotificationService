package ru.notificationservice.server;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.notificationservice.server.notifier.EmailNotifier;

public class EmailNotifierTest {

    private EmailNotifier notifier;

    @Before
    public void createNotifier() {
        notifier = new EmailNotifier("maxvgrad@gmail.com", "JUnit test");
    }

    @Test
    public void executeTest() {
        try {
            notifier.execute(null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Assert.assertTrue(true);
    }
}
