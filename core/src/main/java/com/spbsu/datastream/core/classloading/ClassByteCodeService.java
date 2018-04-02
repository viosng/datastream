package com.spbsu.datastream.core.classloading;

import com.spbsu.datastream.core.data.DSType;

/**
 * @author nickolaysaveliev
 * @since 07/03/2018
 */
public interface ClassByteCodeService {
    byte[] getByteCode(String className);

    void store(String bundle, String name, byte[] bytes);

    void storeDSOperation(String bundle, String name, byte[] bytes, boolean isGroup, DSType from, DSType to, Integer window);
}
