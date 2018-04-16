package com.spbsu.datastream.client;

import com.spbsu.datastream.client.shade.CustomShader;
import com.spbsu.datastream.core.classloading.RemoteClassByteCodeService;
import com.spbsu.datastream.core.classloading.RemoteClassLoader;
import com.spbsu.datastream.core.data.DSItemImpl;
import com.spbsu.datastream.core.data.DSType;
import com.spbsu.datastream.core.operation.DSGroup;
import com.spbsu.datastream.core.operation.DSMap;
import com.spbsu.datastream.core.operation.DSOperation;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.shade.ShadeRequest;
import org.apache.maven.plugins.shade.filter.SimpleFilter;
import org.apache.maven.plugins.shade.relocation.SimpleRelocator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static java.util.Collections.*;

public class Main {

    private static final Set<String> excludedClasses = singleton("com/spbsu/datastream");

    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if (args.length < 6) {
            try(RemoteClassByteCodeService classByteCodeService = new RemoteClassByteCodeService("localhost", 11111)) {
                Class<?> aClass = new RemoteClassLoader(classByteCodeService)
                        .loadClass("bundle-a9fe3f6a-1aa0-4cd4-91bd-a59f91dffd19.com.spbsu.ds.examples.UpperCaseMap");
                Object instance = aClass.newInstance();
                if (instance instanceof DSMap) {
                    DSMap dsMap = (DSMap) instance;
                    DSItemImpl item = new DSItemImpl(DSType.of("string"), Collections.singletonMap("value", "test"));
                    dsMap.map(item).forEach(System.out::println);
                }

                System.out.println(aClass.getName());
            }

            System.out.println("Usage: <host> <port> <path to jar file> <package to include>");
        } else {
            String host = args[0];
            int port = Integer.parseInt(args[1]);
            String jarFileName = args[2];
            String packageName = args[3];
            importJar(host, port, jarFileName, packageName);
        }
    }

    private static void importJar(String host, int port, String fileName, String packageName) {
        try (final RemoteClassByteCodeService byteCodeService = new RemoteClassByteCodeService(host, port)) {

            byteCodeService.processBundle(bundle -> {
                File shadeJarFile = null;
                try {
                    shadeJarFile = shade(bundle, fileName, packageName);

                    final List<String> classes = new ArrayList<>();
                    final URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{shadeJarFile.toURI().toURL()});
                    try(final JarFile jarFile = new JarFile(shadeJarFile.getName())) {
                        final Enumeration<JarEntry> entries = jarFile.entries();
                        while (entries.hasMoreElements()) {
                            final JarEntry je = entries.nextElement();
                            if (je.isDirectory()
                                    || !je.getName().endsWith(".class")
                                    || excludedClasses.stream().anyMatch(prefix -> je.getName().startsWith(prefix))) {
                                continue;
                            }
                            classes.add(je.getName());
                        }
                    }
                    classes.forEach(name -> load(byteCodeService, urlClassLoader, bundle, name));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    if (shadeJarFile != null) {
                        boolean delete = shadeJarFile.delete();
                        if (delete) {
                            System.out.printf("shaded temp jar %s was deleted\n", shadeJarFile.getName());
                        } else {
                            System.err.printf("Can't delete file %s\n", shadeJarFile.getName());
                        }
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static File shade(String bundle, String fileName, String packageName) throws IOException, MojoExecutionException {
        CustomShader shader = new CustomShader();
        ShadeRequest shadeRequest = new ShadeRequest();
        Set<File> jars = singleton(new File(fileName));
        shadeRequest.setJars(jars);
        String folder = Paths.get(".").toAbsolutePath().normalize().toString();
        shadeRequest.setRelocators(singletonList(new SimpleRelocator(packageName, bundle + "." + packageName, emptyList(), emptyList())));
        shadeRequest.setShadeSourcesContent(false);
        File shadedJarFile = new File(folder, bundle + ".jar");
        shadeRequest.setUberJar(shadedJarFile);

        shadeRequest.setFilters(emptyList());
        shadeRequest.setFilters(singletonList(new SimpleFilter(emptySet(), emptySet(), excludedClasses)));
        shadeRequest.setResourceTransformers(emptyList());
        shader.shade(shadeRequest);
        return shadedJarFile;
    }

    private static void load(RemoteClassByteCodeService byteCodeService, ClassLoader classLoader, String bundle, String name) {
        try(InputStream resourceAsStream = classLoader.getResourceAsStream(name)){
            final String className = name.replace('/', '.').substring(0, name.lastIndexOf('.'));
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
