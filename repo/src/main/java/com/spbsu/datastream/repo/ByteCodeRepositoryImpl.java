package com.spbsu.datastream.repo;

import com.spbsu.datastream.core.classloading.ClassByteCodeService;
import io.grpc.internal.IoUtils;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author nickolaysaveliev
 * @since 07/03/2018
 */
public class ByteCodeRepositoryImpl implements ClassByteCodeService {
    @Override
    public byte[] getByteCode(String className) {
        final ClassLoader classLoader = this.getClass().getClassLoader();
        try {
            final Class<?> aClass = classLoader.loadClass(className);
            final String classAsPath = aClass.getName().replace('.', '/') + ".class";
            final InputStream stream = classLoader.getResourceAsStream(classAsPath);
            return IoUtils.toByteArray(stream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void loadJar(JarFile jarFile) {
        final Enumeration<JarEntry> entries = jarFile.entries();

        while (entries.hasMoreElements()) {
            final JarEntry je = entries.nextElement();
            if(je.isDirectory() || !je.getName().endsWith(".class")){
                continue;
            }

        }
    }
}
