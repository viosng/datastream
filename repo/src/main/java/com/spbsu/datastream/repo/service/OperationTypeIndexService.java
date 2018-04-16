package com.spbsu.datastream.repo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.spbsu.datastream.core.data.DSType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class OperationTypeIndexService implements BundleUploadHandler {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final AtomicBoolean isChanged = new AtomicBoolean();
    private final Map<String, Map<DSType, Map<DSType, List<String>>>> operationVersions = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Map<DSType, Map<DSType, String>>>> tempOperationVersions = new ConcurrentHashMap<>();
    private final ReentrantReadWriteLock saveLock = new ReentrantReadWriteLock();
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final String fileName;

    @Autowired
    public OperationTypeIndexService(@Value("${operation.info.storage}") String fileName) throws IOException {
        this.fileName = fileName;
        readStorage();
    }

    private void readStorage() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            TypeFactory typeFactory = objectMapper.getTypeFactory();
            MapType mapType = typeFactory.constructMapType(
                    ConcurrentHashMap.class,
                    typeFactory.constructType(String.class),
                    typeFactory.constructMapType(
                            ConcurrentHashMap.class,
                            typeFactory.constructType(DSType.class),
                            typeFactory.constructMapType(
                                    ConcurrentHashMap.class,
                                    typeFactory.constructType(DSType.class),
                                    typeFactory.constructCollectionType(ArrayList.class,
                                            String.class))));
            Map<String, Map<DSType, Map<DSType, List<String>>>> storedInfo = objectMapper.readValue(new File(fileName), mapType);
            operationVersions.putAll(storedInfo);
        } catch (FileNotFoundException e) {
            log.warn("No stored operation info was found, skipping");
        }
    }

    @PostConstruct
    private void init() {
        executorService.scheduleWithFixedDelay(() -> {
            if (isChanged.compareAndSet(true, false)) {
                save();
            }
        }, 0,5, TimeUnit.SECONDS);
    }

    public void addBundleOperation(String bundle, String className, DSType from, DSType to, boolean isGroup) {
        final String operationType = isGroup ? "group" : "map";
        log.info("add {} : {}, {}, {}, {}", operationType, className, from, to);
        tempOperationVersions
                .computeIfAbsent(bundle, __ -> new ConcurrentHashMap<>())
                .computeIfAbsent(operationType, __ -> new ConcurrentHashMap<>())
                .computeIfAbsent(from, __ -> new ConcurrentHashMap<>())
                .put(to, className);
    }

    private void addOperation(String operationType, String className, DSType from, DSType to) {
        operationVersions
                .computeIfAbsent(operationType, __ -> new ConcurrentHashMap<>())
                .computeIfAbsent(from, __ -> new ConcurrentHashMap<>())
                .compute(to, (k, v) -> {
                    List<String> list = Optional.ofNullable(v).orElse(new ArrayList<>());
                    list.add(className);
                    return list;
                });
    }

    @Override
    public void onError(String bundle, String message) {
        log.info("onError: {}, message: {}", bundle, message);
        tempOperationVersions.remove(bundle);
    }

    @Override
    public void onComplete(String bundle) {
        log.info("onComplete: {}", bundle);
        final ReentrantReadWriteLock.ReadLock readLock = saveLock.readLock();
        try {
            readLock.lock();
            final Map<String, Map<DSType, Map<DSType, String>>> bundleOperations = tempOperationVersions.remove(bundle);
            if (bundleOperations != null) {
                isChanged.set(!bundleOperations.isEmpty());
                bundleOperations
                        .forEach((operationType, fromMap) ->
                                fromMap.forEach((from, toMap) ->
                                        toMap.forEach((to, name) ->
                                                addOperation(operationType, name, from, to))));
            }
        } finally {
            readLock.unlock();
        }
    }

    private void save() {
        final ReentrantReadWriteLock.WriteLock writeLock = saveLock.writeLock();
        try {
            writeLock.lock();
            log.info("save started");
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(new File(fileName), operationVersions);
            log.info("save finished");
        } catch (Exception e) {
            log.error("Can't store operations info", e);
        } finally {
            writeLock.unlock();
        }
    }

    @PreDestroy
    private void shutdown() {
        executorService.shutdown();
        save();
    }
}
