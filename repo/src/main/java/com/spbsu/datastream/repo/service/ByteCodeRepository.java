package com.spbsu.datastream.repo.service;

import com.spbsu.datastream.core.classloading.ClassByteCodeService;
import com.spbsu.datastream.core.data.DSType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.emptySet;

/**
 * @author nickolaysaveliev
 * @since 07/03/2018
 */
@Service
public class ByteCodeRepository implements ClassByteCodeService, BundleUploadHandler {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final Map<String, Set<String>> bundleClasses = new ConcurrentHashMap<>();
    private final ByteStorageService byteStorageService;
    private final OperationTypeIndexService operationTypeIndexService;


    @Autowired
    public ByteCodeRepository(ByteStorageService byteStorageService, OperationTypeIndexService operationTypeIndexService) {
        this.byteStorageService = byteStorageService;
        this.operationTypeIndexService = operationTypeIndexService;
    }

    @Override
    public byte[] getByteCode(String className) {
        return byteStorageService.find(checkNotNull(className))
                .orElseThrow(() -> new IllegalArgumentException(className + " class wasn't found"));
    }

    @Override
    public void store(String bundle, String name, byte[] bytes) {
        bundleClasses.get(bundle).add(name);
        byteStorageService.put(checkNotNull(name), checkNotNull(bytes));
        log.info("Store bundle: {}, class: {}", bundle, name);
    }

    @Override
    public void storeDSOperation(String bundle, String name, byte[] bytes, boolean isGroup, DSType from, DSType to, Integer window) {
        store(bundle, name, bytes);
        operationTypeIndexService.addBundleOperation(bundle, name, from, to, isGroup);
    }

    @Override
    public void onStart(String bundle) {
        bundleClasses.computeIfAbsent(bundle, __ -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
        operationTypeIndexService.onStart(bundle);
    }

    @Override
    public void onError(String bundle, String message) {
        Optional.ofNullable(bundleClasses.remove(bundle))
                .orElse(emptySet())
                .forEach(byteStorageService::remove);
        operationTypeIndexService.onError(bundle, message);
    }

    @Override
    public void onComplete(String bundle) {
        bundleClasses.remove(bundle);
        operationTypeIndexService.onComplete(bundle);
    }
}
