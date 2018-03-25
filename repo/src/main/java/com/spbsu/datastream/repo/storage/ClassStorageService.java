package com.spbsu.datastream.repo.storage;

import com.github.benmanes.caffeine.cache.*;
import org.apache.commons.lang3.SerializationUtils;
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

    private final LoadingCache<String, Class<?>> classCache;

    public ClassStorageService(@NotNull String directory) {
        FSCacheHandler cacheHandler = new FSCacheHandler(directory);
        this.classCache = Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .writer(cacheHandler)
                .build(cacheHandler);
    }

    public Optional<Class<?>> find(@NotNull String className) {
        return Optional.ofNullable(this.classCache.get(className));
    }

    public void storeClass(@NotNull Class<?> clazz) {
        this.classCache.put(clazz.getName(), clazz);
    }

    private static final class FSCacheHandler implements CacheWriter<String, Class<?>>, CacheLoader<String, Class<?>> {

        private final String directory;

        private FSCacheHandler(String directory) {
            this.directory = directory;
        }

        @Override
        public void write(@Nonnull String name, @Nonnull Class<?> aClass) {
            try(OutputStream out = new FileOutputStream(new File(directory, name))) {
                final byte[] bytes = SerializationUtils.serialize(aClass);
                out.write(bytes);
            } catch (IOException e) {
                log.error("Can't store byte code for class " + aClass.getName(), e);
            } catch (Exception e) {
                log.error("Can't serialize class " + aClass.getName(), e);
            }
        }

        @Override
        public void delete(@Nonnull String s, @Nullable Class<?> aClass, @Nonnull RemovalCause removalCause) {
            // no op
        }

        @Nullable
        @Override
        public Class<?> load(@Nonnull String name) {
            try(InputStream in = new FileInputStream(new File(directory, name))) {
                return ((Class<?>) SerializationUtils.deserialize(in));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
