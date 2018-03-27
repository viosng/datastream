package com.spbsu.datastream.repo;

import com.spbsu.datastream.core.classloading.ClassByteCodeService;
import com.spbsu.datastream.core.classloading.RemoteClassLoaderServer;
import com.spbsu.datastream.repo.storage.ClassStorageService;

import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws Exception {
        ClassStorageService storageService = new ClassStorageService(Paths.get(".").toAbsolutePath().normalize().toString() + "\\class-repo");
        ClassByteCodeService byteCodeService = new ByteCodeRepositoryImpl(storageService);
        RemoteClassLoaderServer server = new RemoteClassLoaderServer(byteCodeService, 11111);
        server.start();
        server.blockUntilShutdown();
        storageService.stop();
    }
}


