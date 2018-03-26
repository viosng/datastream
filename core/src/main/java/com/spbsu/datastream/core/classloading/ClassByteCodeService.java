package com.spbsu.datastream.core.classloading;

/**
 * @author nickolaysaveliev
 * @since 07/03/2018
 */
public interface ClassByteCodeService {
    byte[] getByteCode(String className);

    void store(String name, byte[] bytes);

    void store(DSOperationTemplate operationTemplate);
}
