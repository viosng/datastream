package com.spbsu.datastream.client;

import com.spbsu.datastream.client.shade.CustomShader;
import com.spbsu.datastream.core.classloading.RemoteClassByteCodeService;
import com.spbsu.datastream.core.classloading.RemoteClassLoader;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.shade.ShadeRequest;
import org.apache.maven.plugins.shade.relocation.SimpleRelocator;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static java.util.Collections.*;

public class Main {
    public static void main(String[] args) throws ClassNotFoundException {
        if (args.length < 6) {
            Class<?> aClass = new RemoteClassLoader(new RemoteClassByteCodeService("localhost", 11111))
                    .loadClass("bundle-9f960c75-84ba-4df5-8273-0a955f67ae4dcom.google.common.annotations.GwtCompatible");
            System.out.println(aClass.getName());
            System.out.println("Usage: <host> <port> <path to jar file>");
        } else {
            String host = args[0];
            int port = Integer.parseInt(args[1]);
            String jarFileName = args[2];
            importJar(host, port, jarFileName);
        }
    }

    private static File shade(String fileName, UUID uuid) throws IOException, MojoExecutionException {
        CustomShader shader = new CustomShader();
        ShadeRequest shadeRequest = new ShadeRequest();
        shadeRequest.setJars(singleton(new File(fileName)));
        String prefix = "bundle-" + uuid.toString();
        String folder = Paths.get(".").toAbsolutePath().normalize().toString();
        shadeRequest.setRelocators(singletonList(new SimpleRelocator("", prefix, emptyList(), Arrays.asList(
                "java/**",
                "sun/**",
                "jdk/**"
        ))));
        shadeRequest.setShadeSourcesContent(false);
        File shadedJarFile = new File(folder, prefix + ".jar");
        shadeRequest.setUberJar(shadedJarFile);
        shadeRequest.setFilters(emptyList());
        shadeRequest.setResourceTransformers(emptyList());
        shader.shade(shadeRequest);
        return shadedJarFile;
    }

    private static void importJar(String host, int port, String fileName) {
        try (final RemoteClassByteCodeService byteCodeService = new RemoteClassByteCodeService(host, port)) {

            final UUID uuid = UUID.randomUUID();
            final File shadeJarFile = shade(fileName, uuid);

            try(final JarFile jarFile = new JarFile(shadeJarFile.getName())) {
                final URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{shadeJarFile.toURI().toURL()});

                final Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    final JarEntry je = entries.nextElement();
                    if (je.isDirectory() || !je.getName().endsWith(".class")) {
                        continue;
                    }
                    final String className = je.getName().replace('/', '.'); // including ".class"
                    final String restoredClassName = className.substring(0, className.length() - ".class".length());
                    final Class<?> aClass = urlClassLoader.loadClass(restoredClassName);
                    byteCodeService.store(className, SerializationUtils.serialize(aClass));
                }
            } finally {
                if (!(shadeJarFile.delete())) {
                    System.err.println("Can't delete file " + shadeJarFile.getName());
                }
            }
        } catch (IOException | MojoExecutionException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


}
