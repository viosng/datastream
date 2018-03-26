package com.spbsu.datastream.core.classloading;

import com.spbsu.datastream.core.*;
import com.spbsu.datastream.core.Void;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.google.protobuf.ByteString.copyFrom;

public class RemoteClassLoaderServer {
    private static final Logger log = LoggerFactory.getLogger(RemoteClassLoaderServer.class);

    private final Server server;

    public RemoteClassLoaderServer(ClassByteCodeService byteCodeService, int port) {
        server = ServerBuilder.forPort(port)
                .addService(new RemoteClassLoaderServiceImpl(byteCodeService))
                .build();
    }

    public void start() throws IOException {
        server.start();
        log.info("Server started, listening on {}", server.getPort());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.error("*** shutting down gRPC server since JVM is shutting down");
            RemoteClassLoaderServer.this.stop();
            log.error("*** server shut down");
        }));
    }

    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    static class RemoteClassLoaderServiceImpl extends RemoteClassLoaderServiceGrpc.RemoteClassLoaderServiceImplBase {

        private final ClassByteCodeService byteCodeService;

        RemoteClassLoaderServiceImpl(ClassByteCodeService byteCodeService) {
            this.byteCodeService = byteCodeService;
        }

        @Override
        public void findClass(ClassByteCodeRequest request, StreamObserver<ClassByteCodeResponse> responseObserver) {
            try {
                final ClassByteCodeResponse response = ClassByteCodeResponse.newBuilder()
                        .setByteCode(copyFrom(byteCodeService.getByteCode(request.getName())))
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } catch (Exception e) {
                log.error("Error caught", e);
                responseObserver.onError(e);
            }
        }

        @Override
        public void uploadClass(ClassByteCodeUploadRequest request, StreamObserver<Void> responseObserver) {
            try {
                byteCodeService.store(request.getName(), request.getByteCode().toByteArray());
                responseObserver.onNext(Void.newBuilder().build());
                responseObserver.onCompleted();
            } catch (Exception e) {
                log.error("Error caught", e);
                responseObserver.onError(e);
            }
        }
    }
}
