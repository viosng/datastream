package com.spbsu.datastream.repo;

public class Runner implements Runnable {

    @Override
    public void run() {
        System.out.println("Hello from other world!");
        new Secret().print();
    }

    private static final class Secret {
        private void print() {
            System.out.println("Secret");
        }

    }
}
