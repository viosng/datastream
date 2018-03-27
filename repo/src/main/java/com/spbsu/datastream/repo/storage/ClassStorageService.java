package com.spbsu.datastream.repo.storage;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

public class ClassStorageService {

    private static final Logger log = LoggerFactory.getLogger(ClassStorageService.class);

    private final DB db;

    public ClassStorageService(@NotNull String directory) throws IOException {
        log.info("Starting class storage in directory: {}", directory);
        Options options = new Options();
        options.logger(log::info);
        options.cacheSize(100 * 1048576); // 100MB cache
        options.createIfMissing(true);
        options.blockSize();
        db = factory.open(new File(directory), options);
    }

    public Optional<byte[]> find(@NotNull String className) {
        return Optional.ofNullable(db.get(className.getBytes()));
    }

    public void storeClass(@NotNull String className, @NotNull byte[] bytes) {
        this.db.put(className.getBytes(), bytes);
    }

    public void stop() throws Exception {
        db.close();
    }
}
