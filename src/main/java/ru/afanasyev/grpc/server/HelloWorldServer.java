package ru.afanasyev.grpc.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import ru.afanasyev.grpc.GreeterGrpc;
import ru.afanasyev.grpc.GreeterV2Grpc;
import ru.afanasyev.grpc.HelloRequest;
import ru.afanasyev.grpc.HelloRequestV2;
import ru.afanasyev.grpc.HelloResponse;
import ru.afanasyev.grpc.HelloResponseV2;

@Slf4j
public class HelloWorldServer {
    private int port = 42420;
    private Server server;

    public void start() throws Exception {
        log.info("Starting the grpc server");

        server = ServerBuilder.forPort(port)
            .addService(new GreeterImpl())
            .addService(new GreeterImplV2())
            .build()
            .start();

        log.info("Server started. Listening on port " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.warn("*** JVM is shutting down. Turning off grpc server as well ***");
            HelloWorldServer.this.stop();
            log.warn("*** shutdown complete ***");
        }));
    }

    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    private class GreeterImpl extends GreeterGrpc.GreeterImplBase {
        @Override
        public void sayHello(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
            HelloResponse response = HelloResponse.newBuilder().setMessage("Hello " + request.getName()).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    private class GreeterImplV2 extends GreeterV2Grpc.GreeterV2ImplBase {
        @Override
        public void sayHelloV2(HelloRequestV2 request, StreamObserver<HelloResponseV2> responseObserver) {
            log.info("Received message: " + request.getCorrelationId());
            HelloResponseV2 response = HelloResponseV2.newBuilder()
                .setCorrelationId(request.getCorrelationId())
                .setHostName(this.getClass().getSimpleName())
                .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}
