package com.spbsu.datastream.core;

import com.google.protobuf.ByteString;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class RemoteClassLoaderServer {
    private static final Logger logger = LoggerFactory.getLogger(RemoteClassLoaderServer.class);

    private final Server server;

    public RemoteClassLoaderServer(ClassLoader classLoader, int port) {
        server = ServerBuilder.forPort(port)
                .addService(new RemoteClassLoaderServiceImpl(classLoader))
                .build();
    }

    public void start() throws IOException {
        server.start();
        logger.info("Server started, listening on {}", server.getPort());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Use stderr here since the logger may have been reset by its JVM shutdown hook.
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

        private final ClassLoader classLoader;

        RemoteClassLoaderServiceImpl(ClassLoader classLoader) {
            this.classLoader = classLoader;
        }

        @Override
        public void findClass(ClassByteCodeRequest request, StreamObserver<ClassByteCodeResponse> responseObserver) {
            try {
                final Class<?> aClass = classLoader.loadClass(request.getName());
                final String classAsPath = aClass.getName().replace('.', '/') + ".class";
                final InputStream stream = classLoader.getResourceAsStream(classAsPath);
                final ClassByteCodeResponse response = ClassByteCodeResponse.newBuilder()
                        .setByteCode(ByteString.readFrom(stream))
                        .build();
                responseObserver.onNext(response);
            } catch (ClassNotFoundException | IOException e) {
                responseObserver.onError(e);
            } finally {
                responseObserver.onCompleted();
            }
        }
    }
}
