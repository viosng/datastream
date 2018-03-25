package com.spbsu.datastream.core.operation;

import com.spbsu.datastream.core.data.DSItem;

import java.util.stream.Stream;

public interface DSMap extends DSOperation {
    Stream<DSItem> map(DSItem item);
}
