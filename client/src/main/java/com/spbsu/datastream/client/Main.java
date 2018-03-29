package com.spbsu.datastream.client;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.shade.ShadeRequest;
import org.apache.maven.plugins.shade.relocation.SimpleRelocator;

import com.spbsu.datastream.client.shade.CustomShader;
import com.spbsu.datastream.core.classloading.RemoteClassByteCodeService;
import com.spbsu.datastream.core.classloading.RemoteClassLoader;
import com.spbsu.datastream.core.util.ByteCodeBundleUtil;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;

public class Main {
    public static void main(String[] args) throws ClassNotFoundException {
        if (args.length < 6) {
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

    private static File shade(String fileName) throws IOException, MojoExecutionException {
        CustomShader shader = new CustomShader();
        ShadeRequest shadeRequest = new ShadeRequest();
        shadeRequest.setJars(singleton(new File(fileName)));
        String prefix = ByteCodeBundleUtil.bundlePrefix() + ".";
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

            final File shadeJarFile = shade(fileName);

            try(final JarFile jarFile = new JarFile(shadeJarFile.getName())) {
                final URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{shadeJarFile.toURI().toURL()});

                final Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    final JarEntry je = entries.nextElement();
                    if (je.isDirectory() || !je.getName().endsWith(".class")) {
                        continue;
                    }
                    try(InputStream resourceAsStream = urlClassLoader.getResourceAsStream(je.getName())){
                        final String className = je.getName().replace('/', '.');
                        byteCodeService.store(className, IOUtils.toByteArray(resourceAsStream));
                    }
                }
            } finally {
                if (!(shadeJarFile.delete())) {
                    System.err.println("Can't delete file " + shadeJarFile.getName());
                }
            }
        } catch (IOException | MojoExecutionException e) {
            e.printStackTrace();
        }
    }


}
