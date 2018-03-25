package com.spbsu.datastream.repo;

import com.spbsu.datastream.core.classloading.ClassByteCodeService;
import com.spbsu.datastream.core.classloading.DSMapTemplate;
import com.spbsu.datastream.core.classloading.RemoteClassLoaderServer;
import com.spbsu.datastream.core.data.DSItem;
import com.spbsu.datastream.core.data.DSType;
import com.spbsu.datastream.core.operation.DSMap;

import java.io.IOException;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        ClassByteCodeService byteCodeService = new ByteCodeRepositoryImpl(classStorageService);
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


