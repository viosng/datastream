package com.spbsu.datastream.repo.service;

import com.spbsu.datastream.core.data.DSType;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class OperationTypeIndexService extends AbstractBundleUploadHandler {

    private final AtomicBoolean isChanged = new AtomicBoolean();
    private final Map<String, Map<DSType, Map<DSType, List<String>>>> operationVersions = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Map<DSType, Map<DSType, String>>>> tempOperationVersions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService executorService;

    public OperationTypeIndexService() {
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.schedule(() -> {
            if (isChanged.compareAndSet(true, false)) {
                save(); // todo lock
            }
        }, 5, TimeUnit.SECONDS);
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
    public void onStart(String bundle) {
        super.onStart(bundle);
    }

    @Override
    public void onError(String bundle, String message) {
        super.onError(bundle, message);
        tempOperationVersions.remove(bundle);
    }

    @Override
    public void onComplete(String bundle) {
        super.onComplete(bundle);
        final Map<String, Map<DSType, Map<DSType, String>>> bundleOperations = tempOperationVersions.remove(bundle);
        isChanged.set(!bundleOperations.isEmpty());
        bundleOperations
                .forEach((operationType, fromMap) ->
                        fromMap.forEach((from, toMap) ->
                                toMap.forEach((to, name) ->
                                        addOperation(operationType, name, from, to))));
    }

    private void save() {
        //todo
    }

    @PreDestroy
    public void close() {
        save();
    }
}
