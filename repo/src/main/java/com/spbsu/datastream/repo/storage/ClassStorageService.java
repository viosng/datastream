package com.spbsu.datastream.repo.storage;

import com.github.benmanes.caffeine.cache.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class ClassStorageService {

    private static final Logger log = LoggerFactory.getLogger(ClassStorageService.class);

    private final LoadingCache<String, byte[]> classCache;

    public ClassStorageService(@NotNull String directory) {
        new File(directory).mkdirs();
        log.info("Starting class storage in directory: {}", directory);
        FSCacheHandler cacheHandler = new FSCacheHandler(directory);
        this.classCache = Caffeine.newBuilder()
                .maximumSize(500_000)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .writer(cacheHandler)
                .build(cacheHandler);
    }

    public Optional<byte[]> find(@NotNull String className) {
        return Optional.ofNullable(this.classCache.get(className));
    }

    public void storeClass(@NotNull String className, @NotNull byte[] bytes) {
        this.classCache.put(className, bytes);
    }

    private static final class FSCacheHandler implements CacheWriter<String,byte[]>, CacheLoader<String, byte[]> {

        private final String directory;

        private FSCacheHandler(String directory) {
            this.directory = directory;
        }

        @Override
        public void write(@Nonnull String name, @Nonnull byte[] bytes) {
            String className = name.endsWith(".class") ? name : name + ".class";
            try(OutputStream out = new FileOutputStream(new File(directory, className))) {
                out.write(bytes);
                out.flush();
                log.info("Saved class {} with bytes number: {}", name, bytes.length);
            } catch (Exception e) {
                log.error("Can't store byte code for class " + name, e);
            }
        }

        @Override
        public void delete(@Nonnull String s, @Nullable byte[] bytes, @Nonnull RemovalCause removalCause) {
            // no op
        }

        @Nullable
        @Override
        public byte[] load(@Nonnull String name) {
            String className = name.endsWith(".class") ? name : name + ".class";
            try(InputStream in = new FileInputStream(new File(directory, className))) {
                return in.readAllBytes();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
