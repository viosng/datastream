package com.spbsu.datastream.core.classloading;

import com.google.protobuf.ByteString;
import com.spbsu.datastream.core.ClassByteCodeRequest;
import com.spbsu.datastream.core.ClassByteCodeResponse;
import com.spbsu.datastream.core.ClassByteCodeUploadRequest;
import com.spbsu.datastream.core.RemoteClassLoaderServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class RemoteClassByteCodeService implements ClassByteCodeService, AutoCloseable{

    private static final Logger log = LoggerFactory.getLogger(RemoteClassByteCodeService.class);

    private final ManagedChannel channel;
    private final RemoteClassLoaderServiceGrpc.RemoteClassLoaderServiceBlockingStub blockingStub;

    public RemoteClassByteCodeService(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext(true)
                .build());
    }

    RemoteClassByteCodeService(ManagedChannel channel) {
        this.channel = channel;
        blockingStub = RemoteClassLoaderServiceGrpc.newBlockingStub(channel);
    }

    @Override
    public byte[] getByteCode(String className) {
        log.info("get bytecode request: {}", className);
        final ClassByteCodeRequest request = ClassByteCodeRequest.newBuilder().setName(className).build();
        final ClassByteCodeResponse response = blockingStub.findClass(request);
        return response.getByteCode().toByteArray();
    }

    @Override
    public void store(String name, byte[] bytes) {
        log.info("store class request: {}", name);
        final ClassByteCodeUploadRequest request = ClassByteCodeUploadRequest.newBuilder()
                .setName(name)
                .setByteCode(ByteString.copyFrom(bytes))
                .build();
        blockingStub.uploadClass(request);
    }

    @Override
    public void store(DSOperationTemplate operationTemplate) {

    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    @Override
    public void close() {
        try {
            shutdown();
        } catch (InterruptedException e) {
            log.error("Shutdown was interrupted", e);
        }
    }
}
