package com.spbsu.ds.examples;

import com.spbsu.datastream.core.data.DSItem;
import com.spbsu.datastream.core.data.DSItemImpl;
import com.spbsu.datastream.core.data.DSType;
import com.spbsu.datastream.core.operation.DSMap;

import java.util.Collections;
import java.util.stream.Stream;

public class UpperCaseMap implements DSMap {
    @Override
    public Stream<DSItem> map(DSItem item) {
        System.out.println("123 " + item);
        return item.find("value", String.class)
                .map(String::toUpperCase)
                .map(v -> (DSItem) new DSItemImpl(DSType.of("STRING"), Collections.singletonMap("value", v)))
                .map(Stream::of)
                .orElse(Stream.empty());
    }

    @Override
    public DSType fromType() {
        return DSType.of("string");
    }

    @Override
    public DSType toType() {
        return DSType.of("string");
    }
}
