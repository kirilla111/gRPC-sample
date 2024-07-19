package ru.afanasyev.grpc.client;

import com.google.protobuf.Timestamp;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.afanasyev.grpc.BigRequest;
import ru.afanasyev.grpc.BigRequesterGrpc;
import ru.afanasyev.grpc.BigResponse;
import ru.afanasyev.grpc.ListValue;
import ru.afanasyev.grpc.MapValue;
import ru.afanasyev.grpc.RequestEnum;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
public class BigRequestClient {
    private final ManagedChannel channel;
    private final BigRequesterGrpc.BigRequesterBlockingStub blockingStub;
    private final BigRequesterGrpc.BigRequesterStub stub;
    private StreamObserver<BigRequest> bigRequestStreamObserver;
    private StreamResponseObserver streamResponseObserver;

    public BigRequestClient(String hostname, int port) {
        channel = ManagedChannelBuilder.forAddress(hostname, port)
            .usePlaintext()
            .build();
        blockingStub = BigRequesterGrpc.newBlockingStub(channel);
        stub = BigRequesterGrpc.newStub(channel);
    }

    public void initStreamChannel(CountDownLatch countDownLatch) {
        streamResponseObserver = new StreamResponseObserver(countDownLatch);
        bigRequestStreamObserver = stub.callBigRequestStream(streamResponseObserver);
    }


    public void shutdown() throws InterruptedException {
        Optional.ofNullable(streamResponseObserver).ifPresent(StreamResponseObserver::onCompleted);
        Optional.ofNullable(bigRequestStreamObserver).ifPresent(StreamObserver::onCompleted);
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void sendRequest() {
        log.info("Trying to send bigRequest");
        try {
            BigRequest request = getBigRequest();
            BigResponse response = blockingStub.callBigRequest(request);
            log.info("Response: " + response.toString());
        } catch (RuntimeException e) {
            log.error("Request to grpc server failed", e);
        }
    }

    public void sendRequestStream() {
        log.info("Trying to send bigRequest stream");
        try {
            BigRequest request = getBigRequest();
            bigRequestStreamObserver.onNext(request);
        } catch (RuntimeException e) {
            log.error("Request to grpc server failed", e);
        }
    }

    private BigRequest getBigRequest() {
        return BigRequest.newBuilder()
            .setCorrelationId(UUID.randomUUID().toString())
            .setBoolField(true)
            .setEnumField(RequestEnum.OK)
            .setFloatField(1.1f)
            .setTimestampField(Timestamp.newBuilder().setSeconds(1000).build())
            .setListField(ListValue.newBuilder().addIntField(1).addIntField(2).build())
            .setMapField(MapValue.newBuilder().putFields("key", "value").build())
            .build();
    }

    @RequiredArgsConstructor
    class StreamResponseObserver implements StreamObserver<BigResponse> {
        private final CountDownLatch countDownLatch;

        @Override
        public void onNext(BigResponse value) {
            log.info("Received message: " + value.toString());
            countDownLatch.countDown();
        }

        @Override
        public void onError(Throwable t) {
            log.error("Error: ", t);
        }

        @Override
        public void onCompleted() {
            log.info("Stream observation complete");
        }
    }
}
