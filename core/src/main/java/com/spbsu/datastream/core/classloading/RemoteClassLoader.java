package com.spbsu.datastream.core.classloading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteClassLoader extends ClassLoader {
    private static final Logger log = LoggerFactory.getLogger(RemoteClassLoader.class);

    private final ClassByteCodeService classByteCodeService;

    public RemoteClassLoader(ClassByteCodeService classByteCodeService) {
        this.classByteCodeService = classByteCodeService;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        log.info("load class {}", name);
        try {
            final byte[] bytes = classByteCodeService.getByteCode(name);
            return defineClass(name, bytes, 0, bytes.length);
        } catch (Exception e) {
            throw new ClassNotFoundException("Class " + name + " wasn't found", e);
        }
    }
}
