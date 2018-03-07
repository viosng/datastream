package com.spbsu.datastream.repo;

import com.spbsu.datastream.core.classloading.RemoteClassLoaderServer;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        RemoteClassLoaderServer server = new RemoteClassLoaderServer(new ByteCodeRepositoryImpl(), 11111);
        server.start();
        server.blockUntilShutdown();
    }
}
