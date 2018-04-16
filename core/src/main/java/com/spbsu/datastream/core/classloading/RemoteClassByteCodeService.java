package com.spbsu.datastream.core.classloading;

import com.google.protobuf.ByteString;
import com.spbsu.datastream.core.BundleUploadRequest;
import com.spbsu.datastream.core.ClassByteCodeData;
import com.spbsu.datastream.core.ClassByteCodeRequest;
import com.spbsu.datastream.core.RemoteClassLoaderServiceGrpc;
import com.spbsu.datastream.core.data.DSType;
import com.spbsu.datastream.core.util.ByteCodeBundleUtil;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

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

    public void processBundle(Consumer<String> bundleNameConsumer) {
        final String bundle = ByteCodeBundleUtil.bundlePrefix();
        blockingStub.bundleUploadStatus(BundleUploadRequest.newBuilder()
                .setName(bundle)
                .setType(BundleUploadRequest.Type.START)
                .build());
        try {
            bundleNameConsumer.accept(bundle);
            blockingStub.bundleUploadStatus(BundleUploadRequest.newBuilder()
                    .setName(bundle)
                    .setType(BundleUploadRequest.Type.FINISH)
                    .build());
        } catch (Exception e) {
            blockingStub.bundleUploadStatus(BundleUploadRequest.newBuilder()
                    .setName(bundle)
                    .setType(BundleUploadRequest.Type.ERROR)
                    .setMessage(e.getMessage())
                    .build());
            throw e;
        }
    }

    @Override
    public byte[] getByteCode(String className) {
        log.info("get bytecode request: {}", className);
        final ClassByteCodeRequest request = ClassByteCodeRequest.newBuilder().setName(className).build();
        final ClassByteCodeData response = blockingStub.findClass(request);
        return response.getByteCode().toByteArray();
    }

    @Override
    public void store(String bundle, String name, byte[] bytes) {
        log.info("store class request: {}", name);
        final ClassByteCodeData request = ClassByteCodeData.newBuilder()
                .setBundle(bundle)
                .setName(name)
                .setByteCode(ByteString.copyFrom(bytes))
                .setType(ClassByteCodeData.Type.USUAL)
                .build();
        blockingStub.uploadClass(request);
    }

    @Override
    public void storeDSOperation(String bundle, String name, byte[] bytes, boolean isGroup, DSType from, DSType to, Integer window) {
        log.info("store ds operation request: {}", name);
        final ClassByteCodeData.Builder builder = ClassByteCodeData.newBuilder()
                .setBundle(bundle)
                .setName(name)
                .setByteCode(ByteString.copyFrom(bytes))
                .setType(isGroup ? ClassByteCodeData.Type.GROUP : ClassByteCodeData.Type.MAP)
                .setFrom(from.name())
                .setTo(to.name());

        final ClassByteCodeData request = (isGroup ? builder.setWindow(window) : builder).build();
        blockingStub.uploadClass(request);
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
