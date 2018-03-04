package com.spbsu.datastream.repo;

import com.spbsu.datastream.core.RemoteClassLoaderServer;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        RemoteClassLoaderServer server = new RemoteClassLoaderServer(Main.class.getClassLoader(), 11111);
        server.start();
        server.blockUntilShutdown();
    }
}
