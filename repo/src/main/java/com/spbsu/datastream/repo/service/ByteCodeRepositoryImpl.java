package com.spbsu.datastream.repo.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spbsu.datastream.core.classloading.ClassByteCodeService;
import com.spbsu.datastream.core.classloading.DSOperationTemplate;
import com.spbsu.datastream.core.data.DSType;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author nickolaysaveliev
 * @since 07/03/2018
 */
@Service
public class ByteCodeRepositoryImpl implements ClassByteCodeService {

    private final Map<DSType, Map<DSType, Map<Integer, Class<?>>>> operationRepo = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> latestVersions = new ConcurrentHashMap<>();
    private final ClassStorageService classStorageService;

    @Autowired
    public ByteCodeRepositoryImpl(ClassStorageService classStorageService) {
        this.classStorageService = classStorageService;
    }

    @Override
    public byte[] getByteCode(String className) {
        return classStorageService.find(checkNotNull(className))
                .orElseThrow(() -> new IllegalArgumentException(className + " class wasn't found"));
    }

    @Override
    public void store(String name, byte[] bytes) {
        classStorageService.storeClass(checkNotNull(name), checkNotNull(bytes) );
    }

    @Override
    public void store(DSOperationTemplate template) {
        /*final DSOperationTemplate operationTemplate = checkNotNull(template);
        latestVersions.compute(operationName(operationTemplate), (operationName, counter) -> {
            final AtomicInteger versionCounter = Optional.ofNullable(counter).orElse(new AtomicInteger());
            final int newVersion = versionCounter.incrementAndGet();
            final String nameWithVersion = operationNameWithVersion(operationTemplate, newVersion);
            if (operationTemplate instanceof DSMapTemplate) {
                operationRepo.put(nameWithVersion, generateMap(nameWithVersion, ((DSMapTemplate) operationTemplate)));
            } else if (operationTemplate instanceof DSGroupTemplate) {
                operationRepo.put(nameWithVersion, generateGroup(nameWithVersion, ((DSGroupTemplate) operationTemplate)));
            }
            return versionCounter;
        });*/
    }

    private String operationName(DSOperationTemplate operation) {
        return String.format("%sTo%s", operation.inputType().name(), operation.outputType().name());
    }

    private String operationNameWithVersion(DSOperationTemplate operation, int version) {
        return String.format("%s$%d", operationName(operation), version);
    }

}
