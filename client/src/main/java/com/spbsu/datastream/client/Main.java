package com.spbsu.datastream.client;

import com.spbsu.datastream.client.shade.CustomShader;
import com.spbsu.datastream.core.classloading.RemoteClassByteCodeService;
import com.spbsu.datastream.core.classloading.RemoteClassLoader;
import com.spbsu.datastream.core.operation.DSGroup;
import com.spbsu.datastream.core.operation.DSOperation;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.shade.ShadeRequest;
import org.apache.maven.plugins.shade.relocation.SimpleRelocator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static java.util.Collections.*;

public class Main {
    public static void main(String[] args) throws ClassNotFoundException {
        if (args.length < 3) {
            Class<?> aClass = new RemoteClassLoader(new RemoteClassByteCodeService("localhost", 11111))
                    .loadClass("bundle-356fa7b1-e458-4255-a811-b1c09fef67d7.org.apache.tools.ant.types.selectors.TypeSelector.class");
            System.out.println(aClass.getName());
            System.out.println("Usage: <host> <port> <path to jar file>");
        } else {
            String host = args[0];
            int port = Integer.parseInt(args[1]);
            String jarFileName = args[2];
            importJar(host, port, jarFileName);
        }
    }

    private static void importJar(String host, int port, String fileName) {
        try (final RemoteClassByteCodeService byteCodeService = new RemoteClassByteCodeService(host, port)) {

            byteCodeService.processBundle(bundle -> {
                try {
                    final File shadeJarFile = shade(bundle, fileName);

                    try(final JarFile jarFile = new JarFile(shadeJarFile.getName())) {
                        final URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{shadeJarFile.toURI().toURL()});

                        final Enumeration<JarEntry> entries = jarFile.entries();

                        final List<String> classes = new ArrayList<>();
                        while (entries.hasMoreElements()) {
                            final JarEntry je = entries.nextElement();
                            if (je.isDirectory() || !je.getName().endsWith(".class")) {
                                continue;
                            }
                            classes.add(je.getName());
                        }

                        classes.parallelStream().forEach(name -> load(byteCodeService, urlClassLoader, bundle, name));
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!(new File(fileName).delete())) {
            System.err.println("Can't delete file " + fileName);
        }
    }

    private static File shade(String bundle, String fileName) throws IOException, MojoExecutionException {
        CustomShader shader = new CustomShader();
        ShadeRequest shadeRequest = new ShadeRequest();
        shadeRequest.setJars(singleton(new File(fileName)));
        String folder = Paths.get(".").toAbsolutePath().normalize().toString();
        shadeRequest.setRelocators(singletonList(new SimpleRelocator("", bundle + ".", emptyList(), Arrays.asList(
                "java/**",
                "sun/**",
                "jdk/**",
                "com.spbsu.datastream/**"
        ))));
        shadeRequest.setShadeSourcesContent(false);
        File shadedJarFile = new File(folder, bundle + ".jar");
        shadeRequest.setUberJar(shadedJarFile);
        shadeRequest.setFilters(emptyList());
        shadeRequest.setResourceTransformers(emptyList());
        shader.shade(shadeRequest);
        return shadedJarFile;
    }

    private static void load(RemoteClassByteCodeService byteCodeService, ClassLoader classLoader, String bundle, String name) {
        try(InputStream resourceAsStream = classLoader.getResourceAsStream(name)){
            final String className = name.replace('/', '.');
            final byte[] bytes = IOUtils.toByteArray(resourceAsStream);
            final Class<?> aClass = classLoader.loadClass(className);
            if (aClass.isAssignableFrom(DSOperation.class)) {
                DSOperation operation = ((DSOperation) aClass.newInstance());
                if (operation instanceof DSGroup) {
                    byteCodeService.storeDSOperation(bundle, className, bytes, true, operation.fromType(),
                            operation.toType(), ((DSGroup) operation).window());
                } else {
                    byteCodeService.storeDSOperation(bundle, className, bytes, false, operation.fromType(),
                            operation.toType(), null);
                }
            } else {
                byteCodeService.store(bundle, className, bytes);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
