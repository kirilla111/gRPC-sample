package ru.afanasyev.grpc.client;

import lombok.SneakyThrows;
import org.apache.log4j.BasicConfigurator;

import java.util.concurrent.CountDownLatch;

public class ClientApplication {
    public static void main(String[] args) throws Exception {
        BasicConfigurator.configure();
        SimpleClient client = new SimpleClient("localhost", 42420);
        BigRequestClient bigRequestClient = new BigRequestClient("localhost", 42420);
        String name = args.length > 0 ? args[0] : "unknown";

        try {
            sendStream(bigRequestClient);
            sendSimple(bigRequestClient);
            client.greet(name);
            client.greetV2();
        } finally {
            client.shutdown();
            bigRequestClient.shutdown();
        }

    }

    private static void sendSimple(BigRequestClient bigRequestClient) {
        long time = System.currentTimeMillis();
        int streamSize = 10000;
        for (int i = 0; i < streamSize; i++) {
            bigRequestClient.sendRequest();
        }
        System.out.println("sendSimple completed in: " + (System.currentTimeMillis() - time)); // 24332
    }

    @SneakyThrows
    private static void sendStream(BigRequestClient bigRequestClient) {
        long time = System.currentTimeMillis();
        int streamSize = 10000;
        CountDownLatch countDownLatch = new CountDownLatch(streamSize);
        bigRequestClient.initStreamChannel(countDownLatch);
        for (int i = 0; i < streamSize; i++) {
            bigRequestClient.sendRequestStream();
        }
        countDownLatch.await();
        System.out.println("sendStream completed in: " + (System.currentTimeMillis() - time)); // 6082
    }
}
