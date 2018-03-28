package com.spbsu.datastream.repo.service;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import javax.annotation.PreDestroy;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

@Service
public class ClassStorageService {

    private static final Logger log = LoggerFactory.getLogger(ClassStorageService.class);

    private final DB db;

    @Autowired
    public ClassStorageService(@Value("${class.storage.dir}") String directory,
                               @Value("${class.storage.cache.size.bytes}") int cacheSize) throws IOException {
        log.info("Starting class storage in directory: {}", directory);
        Options options = new Options();
        options.logger(log::info);
        options.cacheSize(cacheSize);
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

    @PreDestroy
    public void stop() throws Exception {
        db.close();
    }
}
