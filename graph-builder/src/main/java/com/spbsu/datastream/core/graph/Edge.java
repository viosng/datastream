package com.spbsu.datastream.core.graph;

import com.spbsu.datastream.core.data.DSItem;

import static com.google.common.base.Preconditions.checkArgument;

public abstract class Edge {
    private final Vertex from;
    private final Vertex to;

    protected Edge(Vertex from, Vertex to) {
        checkArgument(from.outputType().equals(to.inputType()), "Output and input types are different: %s an %s",
                from.outputType().name(), to.inputType().name());
        this.from = from;
        this.to = to;
    }

    public abstract void send(DSItem item);
}
