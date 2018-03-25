package com.spbsu.datastream.core.classloading;

import com.spbsu.datastream.core.operation.DSGroup;

public interface DSGroupTemplate extends DSOperationTemplate {
    int window();

    Class<? extends DSGroup> groupClass();
}
