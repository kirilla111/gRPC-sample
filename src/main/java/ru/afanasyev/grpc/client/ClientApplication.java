package ru.afanasyev.grpc.client;

import org.apache.log4j.BasicConfigurator;

public class ClientApplication {
    public static void main(String[] args) throws Exception {
        BasicConfigurator.configure();
        SimpleClient client = new SimpleClient("localhost", 42420);
        BigRequestClient bigRequestClient = new BigRequestClient("localhost", 42420);
        String name = args.length > 0 ? args[0] : "unknown";

        try {
            client.greet(name);
            client.greetV2();
            bigRequestClient.sendRequest();
        } finally {
            client.shutdown();
            bigRequestClient.shutdown();
        }
    }
}
