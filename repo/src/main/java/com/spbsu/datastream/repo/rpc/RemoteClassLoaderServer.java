package com.spbsu.datastream.repo.rpc;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.spbsu.datastream.core.ClassByteCodeRequest;
import com.spbsu.datastream.core.ClassByteCodeResponse;
import com.spbsu.datastream.core.ClassByteCodeUploadRequest;
import com.spbsu.datastream.core.RemoteClassLoaderServiceGrpc;
import com.spbsu.datastream.core.Void;
import com.spbsu.datastream.core.classloading.ClassByteCodeService;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import static com.google.protobuf.ByteString.copyFrom;

@Service
public class RemoteClassLoaderServer {
    private static final Logger log = LoggerFactory.getLogger(RemoteClassLoaderServer.class);

    private final Server server;

    @Autowired
    public RemoteClassLoaderServer(ClassByteCodeService byteCodeService, @Value("${class.loader.server.port}") int port) {
        server = ServerBuilder.forPort(port)
                .addService(new RemoteClassLoaderServiceImpl(byteCodeService))
                .build();
    }

    @PostConstruct
    public void start() throws IOException {
        server.start();
        log.info("Server started, listening on {}", server.getPort());
    }

    @PreDestroy
    public void stop() {
        if (server != null) {
            server.shutdown();
            log.info("Server stopped");
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
