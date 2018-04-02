package com.spbsu.datastream.core.operation;

import com.spbsu.datastream.core.data.DSItem;
import com.spbsu.datastream.core.data.DSType;

public interface DSOperation {

    DSType fromType();

    DSType toType();

    default boolean verify(DSItem item) {
        return true;
    }
}
