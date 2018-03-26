package com.spbsu.datastream.repo;

import com.spbsu.datastream.core.classloading.ClassByteCodeService;
import com.spbsu.datastream.core.classloading.DSMapTemplate;
import com.spbsu.datastream.core.classloading.RemoteClassLoaderServer;
import com.spbsu.datastream.core.data.DSItem;
import com.spbsu.datastream.core.data.DSType;
import com.spbsu.datastream.core.operation.DSMap;
import com.spbsu.datastream.repo.storage.ClassStorageService;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        ClassStorageService storageService = new ClassStorageService(Paths.get(".").toAbsolutePath().normalize().toString() + "\\class-repo");
        ClassByteCodeService byteCodeService = new ByteCodeRepositoryImpl(storageService);
        fill(byteCodeService);
        RemoteClassLoaderServer server = new RemoteClassLoaderServer(byteCodeService, 11111);
        server.start();
        server.blockUntilShutdown();

    }

    private static void fill(ClassByteCodeService byteCodeService) {
        byteCodeService.store(new DSMapTemplate() {
            @Override
            public Class<? extends DSMap> mapClass() {
                return CustomDSMap.class;
            }

            @Override
            public DSType inputType() {
                return DSType.of("A");
            }

            @Override
            public DSType outputType() {
                return DSType.of("B");
            }

            class CustomDSMap implements DSMap {

                @Override
                public Stream<DSItem> map(DSItem item) {
                    System.out.println("1");
                    return Stream.of(item);
                }
            }
        });

        byteCodeService.store(new DSMapTemplate() {
            @Override
            public Class<? extends DSMap> mapClass() {
                return CustomDSMap.class;
            }

            @Override
            public DSType inputType() {
                return DSType.of("A");
            }

            @Override
            public DSType outputType() {
                return DSType.of("B");
            }

            class CustomDSMap implements DSMap {

                @Override
                public Stream<DSItem> map(DSItem item) {
                    System.out.println("2");
                    return Stream.of(item);
                }
            }
        });
    }
}


