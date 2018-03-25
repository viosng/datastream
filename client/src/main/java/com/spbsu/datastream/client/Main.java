package com.spbsu.datastream.client;

import com.spbsu.datastream.core.classloading.RemoteClassByteCodeService;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.shade.ShadeRequest;
import org.apache.maven.plugins.shade.relocation.SimpleRelocator;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static java.util.Collections.*;

public class Main {
    public static void main(String[] args)  {
        if (args.length < 3) {
            System.out.println("Usage: <host> <port> <path to jar file>");
        } else {
            String host = args[0];
            int port = Integer.parseInt(args[1]);
            String jarFileName = args[2];
            importJar(host, port, jarFileName);
        }
    }

    private static String shade(String fileName, UUID uuid) throws IOException, MojoExecutionException {
        CustomShader shader = new CustomShader();
        ShadeRequest shadeRequest = new ShadeRequest();
        shadeRequest.setJars(singleton(new File(fileName)));
        shadeRequest.setRelocators(singletonList(new SimpleRelocator("", uuid.toString(), emptyList(), emptyList())));
        shadeRequest.setShadeSourcesContent(false);
        String shadedJarFileName = uuid.toString() + ".jar";
        shadeRequest.setUberJar(new File(shadedJarFileName));
        shadeRequest.setFilters(emptyList());
        shadeRequest.setResourceTransformers(emptyList());
        shader.shade(shadeRequest);
        return shadedJarFileName;
    }


    private static void importJar(String host, int port, String fileName) {
        try (final JarFile jarFile = new JarFile(fileName);
             final RemoteClassByteCodeService byteCodeService = new RemoteClassByteCodeService(host, port)) {

            final UUID uuid = UUID.randomUUID();
            final String shadeJarFileName = shade(fileName, uuid);

            try {
                final File file = new File(shadeJarFileName);
                final URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{file.toURI().toURL()});

                final Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    final JarEntry je = entries.nextElement();
                    if (je.isDirectory() || !je.getName().endsWith(".class")) {
                        continue;
                    }
                    final Class<?> aClass = urlClassLoader.loadClass(je.getName());
                    byteCodeService.store(aClass);
                }
            } finally {
                if (!(new File(shadeJarFileName).delete())) {
                    System.err.println("Can't delete file " + shadeJarFileName);
                }
            }
        } catch (IOException | MojoExecutionException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


}
