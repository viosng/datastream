package com.spbsu.datastream.repo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.spbsu.datastream.repo.rpc.RemoteClassLoaderServer;

@SpringBootApplication
public class Application {
    public static void main(String[] args) throws InterruptedException {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        Runtime.getRuntime().addShutdownHook(new Thread(context::close));
        RemoteClassLoaderServer server = context.getBean(RemoteClassLoaderServer.class);
        server.blockUntilShutdown();
    }
}


