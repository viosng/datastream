package com.spbsu.datastream.core.classloading;

import com.spbsu.datastream.core.operation.DSMap;

public interface DSMapTemplate extends DSOperationTemplate {
    Class<? extends DSMap> mapClass();
}
