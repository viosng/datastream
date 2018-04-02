package com.spbsu.datastream.repo.service;

import com.spbsu.datastream.core.classloading.ClassByteCodeService;
import com.spbsu.datastream.core.data.DSType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author nickolaysaveliev
 * @since 07/03/2018
 */
@Service
public class ByteCodeRepository implements ClassByteCodeService, BundleUploadHandler {

    private final ByteStorageService byteStorageService;
    private final BundleIndexService bundleIndexService;
    private final OperationTypeIndexService operationTypeIndexService;

    @Autowired
    public ByteCodeRepository(ByteStorageService byteStorageService, BundleIndexService bundleIndexService, OperationTypeIndexService operationTypeIndexService) {
        this.byteStorageService = byteStorageService;
        this.bundleIndexService = bundleIndexService;
        this.operationTypeIndexService = operationTypeIndexService;
    }

    @Override
    public byte[] getByteCode(String className) {
        return byteStorageService.find(checkNotNull(className))
                .orElseThrow(() -> new IllegalArgumentException(className + " class wasn't found"));
    }

    @Override
    public void store(String bundle, String name, byte[] bytes) {
        bundleIndexService.put(bundle, name);
        byteStorageService.put(checkNotNull(name), checkNotNull(bytes));
    }

    @Override
    public void storeDSOperation(String bundle, String name, byte[] bytes, boolean isGroup, DSType from, DSType to, Integer window) {
        store(bundle, name, bytes);
        operationTypeIndexService.addBundleOperation(bundle, name, from, to, isGroup);
    }

    @Override
    public void onStart(String bundle) {
        bundleIndexService.onStart(bundle);
        operationTypeIndexService.onStart(bundle);
    }

    @Override
    public void onError(String bundle, String message) {
        bundleIndexService.onError(bundle, message);
        operationTypeIndexService.onError(bundle, message);
    }

    @Override
    public void onComplete(String bundle) {
        bundleIndexService.onComplete(bundle);
        operationTypeIndexService.onComplete(bundle);
    }
}
