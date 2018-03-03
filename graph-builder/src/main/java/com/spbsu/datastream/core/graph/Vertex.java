package com.spbsu.datastream.core.graph;

import com.spbsu.datastream.core.data.DSType;
import com.spbsu.datastream.core.operation.DSOperation;

public abstract class Vertex {
    private final DSOperation operation;

    protected Vertex(DSOperation operation) {
        this.operation = operation;
    }

    public DSType inputType() {
        return operation.inputType();
    }

    public DSType outputType() {
        return operation.outputType();
    }

    private static boolean verifyEdge(Vertex from, Vertex to) {
        return from.operation.outputType().equals(to.operation.inputType());
    }
}
