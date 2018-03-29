package com.spbsu.datastream.core.graph;

import com.spbsu.datastream.core.data.DSItem;

public abstract class Edge {
    private final Vertex from;
    private final Vertex to;

    protected Edge(Vertex from, Vertex to) {
        this.from = from;
        this.to = to;
    }

    public abstract void send(DSItem item);
}
