package com.spbsu.datastream.core.operation;

public interface DSGroup extends DSOperation {
    int window();

    GroupKey groupKey();
}
