package com.spbsu.datastream.core.operation;

import com.spbsu.datastream.core.data.DSItem;
import com.spbsu.datastream.core.data.DSType;

public interface DSOperation {

    DSType inputType();

    DSType outputType();

    default boolean verify(DSItem item) {
        return true;
    }

}
