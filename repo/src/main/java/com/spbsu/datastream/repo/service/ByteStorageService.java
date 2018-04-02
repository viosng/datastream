package com.spbsu.datastream.repo.service;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

@Service
public class ByteStorageService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final DB db;

    @Autowired
    public ByteStorageService(@Value("${class.storage.dir}") String directory,
                              @Value("${class.storage.cache.size.bytes}") int cacheSize) throws IOException {
        log.info("Starting class storage in directory: {}", directory);
        db = factory.open(new File(directory), new Options()
                .logger(log::info)
                .cacheSize(cacheSize)
                .createIfMissing(true));
    }

    public Optional<byte[]> find(@NotNull String className) {
        return Optional.ofNullable(db.get(className.getBytes()));
    }

    public void put(@NotNull String className, @NotNull byte[] bytes) {
        this.db.put(className.getBytes(), bytes);
    }

    public void remove(@NotNull String className) {
        db.delete(className.getBytes());
    }

    @PreDestroy
    public void stop() throws Exception {
        db.close();
    }
}
