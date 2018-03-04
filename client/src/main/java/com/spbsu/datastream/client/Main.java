package com.spbsu.datastream.client;

import com.spbsu.datastream.core.RemoteClassLoader;

public class Main {
    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        RemoteClassLoader classLoader = new RemoteClassLoader("localhost", 11111);
        Runnable runnable = (Runnable) classLoader.loadClass("com.spbsu.datastream.repo.Runner").newInstance();
        runnable.run();
    }
}
