package ru.afanasyev.grpc.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import ru.afanasyev.grpc.GreeterGrpc;
import ru.afanasyev.grpc.GreeterV2Grpc;
import ru.afanasyev.grpc.HelloRequest;
import ru.afanasyev.grpc.HelloRequestV2;
import ru.afanasyev.grpc.HelloResponse;
import ru.afanasyev.grpc.HelloResponseV2;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SimpleClient {
    private final ManagedChannel channel;
    private GreeterGrpc.GreeterBlockingStub blockingStub;
    private GreeterV2Grpc.GreeterV2BlockingStub blockingStubV2;

    public SimpleClient(String hostname, int port) {
        channel = ManagedChannelBuilder.forAddress(hostname, port)
            .usePlaintext()
            .build();
        blockingStub = GreeterGrpc.newBlockingStub(channel);
        blockingStubV2 = GreeterV2Grpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void greet(String name) {
        log.info("Trying to greet " + name);
        try {
            HelloRequest request = HelloRequest.newBuilder().setName(name).build();
            HelloResponse response = blockingStub.sayHello(request);
            log.info("Response: " + response.getMessage());
        } catch (RuntimeException e) {
            log.error("Request to grpc server failed", e);
        }
    }

    public void greetV2() {
        log.info("Trying to greet v2");
        String correlationId = UUID.randomUUID().toString();
        String clientName = this.getClass().getSimpleName();
        try {
            HelloRequestV2 request = HelloRequestV2.newBuilder()
                .setCorrelationId(correlationId)
                .setClientName(clientName).build();
            HelloResponseV2 response = blockingStubV2.sayHelloV2(request);
            log.info("Response correlationId: " + response.getCorrelationId() + " host: " + response.getHostName());
        } catch (RuntimeException e) {
            log.error("Request to grpc server failed", e);
        }
    }
}
