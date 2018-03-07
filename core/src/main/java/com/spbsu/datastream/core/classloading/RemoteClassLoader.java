package com.spbsu.datastream.core.classloading;

import com.spbsu.datastream.core.ClassByteCodeRequest;
import com.spbsu.datastream.core.ClassByteCodeResponse;
import com.spbsu.datastream.core.RemoteClassLoaderServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class RemoteClassLoader extends ClassLoader {
    private static final Logger log = LoggerFactory.getLogger(RemoteClassLoader.class);

    private final ManagedChannel channel;
    private final RemoteClassLoaderServiceGrpc.RemoteClassLoaderServiceBlockingStub blockingStub;

    public RemoteClassLoader(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext(true)
                .build());
    }

    RemoteClassLoader(ManagedChannel channel) {
        this.channel = channel;
        blockingStub = RemoteClassLoaderServiceGrpc.newBlockingStub(channel);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        log.info("load class {}", name);
        final ClassByteCodeRequest request = ClassByteCodeRequest.newBuilder().setName(name).build();
        final ClassByteCodeResponse response;
        try {
            response = blockingStub.findClass(request);
        } catch (Exception e) {
            log.warn("load failed for class name: " + name, e);
            throw new ClassNotFoundException(name, e);
        }
        final byte[] bytes = response.getByteCode().toByteArray();
        return defineClass(name, bytes, 0, bytes.length);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
}
