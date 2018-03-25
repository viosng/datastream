package com.spbsu.datastream.core.operation;

import com.spbsu.datastream.core.data.DSItem;

public interface DSOperation {
    default boolean verify(DSItem item) {
        return true;
    }
}
