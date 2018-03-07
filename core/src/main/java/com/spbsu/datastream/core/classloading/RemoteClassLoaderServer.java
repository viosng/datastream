package com.spbsu.datastream.core.classloading;

import com.spbsu.datastream.core.ClassByteCodeRequest;
import com.spbsu.datastream.core.ClassByteCodeResponse;
import com.spbsu.datastream.core.RemoteClassLoaderServiceGrpc;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.google.protobuf.ByteString.copyFrom;

public class RemoteClassLoaderServer {
    private static final Logger logger = LoggerFactory.getLogger(RemoteClassLoaderServer.class);

    private final Server server;

    public RemoteClassLoaderServer(ClassByteCodeService byteCodeService, int port) {
        server = ServerBuilder.forPort(port)
                .addService(new RemoteClassLoaderServiceImpl(byteCodeService))
                .build();
    }

    public void start() throws IOException {
        server.start();
        logger.info("Server started, listening on {}", server.getPort());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.error("*** shutting down gRPC server since JVM is shutting down");
            RemoteClassLoaderServer.this.stop();
            logger.error("*** server shut down");
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
                responseObserver.onError(e);
            }
        }
    }
}
