package ru.afanasyev.grpc.server;

import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.BasicConfigurator;

import java.util.Arrays;

@Slf4j
public class ServerApplication {
    public static void main(String[] args) throws Exception {
        BasicConfigurator.configure();
        log.info("Server startup. Args = " + Arrays.toString(args));
        final HelloWorldServer helloWorldServer = new HelloWorldServer();

        helloWorldServer.start();
        helloWorldServer.blockUntilShutdown();
    }
}
