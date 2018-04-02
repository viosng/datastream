package com.spbsu.datastream.repo.rpc;

import com.spbsu.datastream.core.*;
import com.spbsu.datastream.core.Void;
import com.spbsu.datastream.core.data.DSType;
import com.spbsu.datastream.repo.service.ByteCodeRepository;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

@Service
public class RemoteClassLoaderServer {
    private static final Logger log = LoggerFactory.getLogger(RemoteClassLoaderServer.class);

    private final Server server;

    @Autowired
    public RemoteClassLoaderServer(ByteCodeRepository byteCodeRepository, @Value("${class.loader.server.port}") int port) {
        server = ServerBuilder.forPort(port)
                .addService(new RemoteClassLoaderServiceImpl(byteCodeRepository))
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

        private final ByteCodeRepository byteCodeRepository;

        RemoteClassLoaderServiceImpl(ByteCodeRepository byteCodeRepository) {
            this.byteCodeRepository = byteCodeRepository;
        }

        @Override
        public void bundleUploadStatus(BundleUploadRequest request, StreamObserver<Void> responseObserver) {
            try {
                switch (request.getType()) {

                    case START:
                        byteCodeRepository.onStart(request.getName());
                        break;
                    case FINISH:
                        byteCodeRepository.onComplete(request.getName());
                        break;
                    case ERROR:
                        byteCodeRepository.onError(request.getName(), request.getMessage());
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown bundle upload type");
                }
                responseObserver.onNext(Void.newBuilder().build());
                responseObserver.onCompleted();
            } catch (Exception e) {
                log.error("Error caught", e);
                responseObserver.onError(e);
            }
        }

        @Override
        public void findClass(ClassByteCodeRequest request, StreamObserver<ClassByteCodeData> responseObserver) {
            try {
                final ClassByteCodeData data = ClassByteCodeData.parseFrom(byteCodeRepository.getByteCode(request.getName()));
                responseObserver.onNext(data);
                responseObserver.onCompleted();
            } catch (Exception e) {
                log.error("Error caught", e);
                responseObserver.onError(e);
            }
        }

        @Override
        public void uploadClass(ClassByteCodeData data, StreamObserver<Void> responseObserver) {
            try {
                final byte[] bytes = data.toByteArray();
                switch (data.getType()) {

                    case USUAL:
                        byteCodeRepository.store(data.getBundle(), data.getName(), bytes);
                        break;
                    case MAP:
                        byteCodeRepository.storeDSOperation(data.getBundle(), data.getName(), bytes,
                                false, DSType.of(data.getFrom()), DSType.of(data.getTo()), null);
                        break;
                    case GROUP:
                        byteCodeRepository.storeDSOperation(data.getBundle(), data.getName(), bytes,
                                true, DSType.of(data.getFrom()), DSType.of(data.getTo()), data.getWindow());
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown class data type");
                }

                responseObserver.onNext(Void.newBuilder().build());
                responseObserver.onCompleted();
            } catch (Exception e) {
                log.error("Error caught", e);
                responseObserver.onError(e);
            }
        }
    }
}
