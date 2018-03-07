package com.spbsu.datastream.core.classloading;

import java.util.jar.JarFile;

/**
 * @author nickolaysaveliev
 * @since 07/03/2018
 */
public interface ClassByteCodeService {
    byte[] getByteCode(String className);

    void loadJar(JarFile jarFile);
}
