package com.spbsu.datastream.core.data;

import java.util.Objects;

public class DSType {
    private final String name;

    private DSType(String name) {
        this.name = name;
    }

    public static DSType of(String name) {
        return new DSType(name);
    }

    public String name() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DSType dsType = (DSType) o;
        return Objects.equals(name, dsType.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
