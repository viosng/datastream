package com.spbsu.datastream.core.graph;

import com.spbsu.datastream.core.operation.DSOperation;

public abstract class Vertex {
    private final DSOperation operation;

    protected Vertex(DSOperation operation) {
        this.operation = operation;
    }
}
