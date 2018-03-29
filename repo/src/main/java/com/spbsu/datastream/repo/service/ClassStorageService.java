package com.spbsu.datastream.repo.service;

import com.github.benmanes.caffeine.cache.*;
import com.google.common.io.Files;
import com.spbsu.datastream.core.util.ByteCodeBundleUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class ClassStorageService {

    private static final Logger log = LoggerFactory.getLogger(ClassStorageService.class);

    private final LoadingCache<String, byte[]> byteCodeCache;


    @Autowired
    public ClassStorageService(@Value("${class.storage.dir}") String directory,
                               @Value("${class.storage.cache.size.bytes}") int cacheSize) {
        new File(directory).mkdirs();
        log.info("Starting class storage in directory: {}", directory);
        FSCacheHandler cacheHandler = new FSCacheHandler(directory);
        this.byteCodeCache = Caffeine.newBuilder()
                .<String, byte[]>weigher((k, v) -> v.length)
                .maximumWeight(cacheSize)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .writer(cacheHandler)
                .build(cacheHandler);
    }

    public Optional<byte[]> find(@NotNull String className) {
        return Optional.ofNullable(byteCodeCache.get(className));
    }

    public void storeClass(@NotNull String className, @NotNull byte[] bytes) {
        byteCodeCache.put(className, bytes);
    }

    private static final class FSCacheHandler implements CacheWriter<String, byte[]>, CacheLoader<String, byte[]> {

        private final String directory;

        private FSCacheHandler(String directory) {
            this.directory = directory;
        }

        @Override
        public void write(@Nonnull String name, @Nonnull byte[] bytes) {
            final String parentDir = directory + "/" + ByteCodeBundleUtil.extractBundlePrefix(name);
            final File file = new File(parentDir, name);
            file.getParentFile().mkdirs();
            try (OutputStream out = new FileOutputStream(file)) {
                out.write(bytes);
            } catch (Exception e) {
                log.error("Can't serialize class " + name, e);
            }
        }

        @Override
        public void delete(@Nonnull String s, @Nullable byte[] aClass, @Nonnull RemovalCause removalCause) {
            // no op
        }

        @Nullable
        @Override
        public byte[] load(@Nonnull String name) {
            final String parentDir = directory + "/" + ByteCodeBundleUtil.extractBundlePrefix(name);
            try {
                return Files.toByteArray(new File(parentDir, name));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
