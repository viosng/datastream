package com.spbsu.datastream.repo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BundleIndexService extends AbstractBundleUploadHandler {
    private final Map<String, Set<String>> bundleClasses = new ConcurrentHashMap<>();

    private final ByteStorageService byteStorageService;

    @Autowired
    public BundleIndexService(ByteStorageService byteStorageService) {
        this.byteStorageService = byteStorageService;
    }

    public void put(String bundle, String key) {
        bundleClasses.get(bundle).add(key);
    }

    @Override
    public void onStart(String bundle) {
        super.onStart(bundle);
        bundleClasses.computeIfAbsent(bundle, __ -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
    }

    @Override
    public void onError(String bundle, String message) {
        super.onError(bundle, message);
        bundleClasses.remove(bundle).forEach(byteStorageService::remove);
    }

    @Override
    public void onComplete(String bundle) {
        super.onComplete(bundle);
        bundleClasses.remove(bundle);
    }
}
