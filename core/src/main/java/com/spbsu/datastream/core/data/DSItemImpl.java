package com.spbsu.datastream.core.data;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.Optional;

public class DSItemImpl implements DSItem {

    private final DSType type;
    private final Map<String, Object> data;

    public DSItemImpl(DSType type, Map<String, Object> data) {
        this.type = type;
        this.data = ImmutableMap.copyOf(data);
    }

    @Override
    public DSType type() {
        return type;
    }

    @Override
    public <T> T get(String key, Class<T> tClass) {
        return tClass.cast(data.get(key));
    }

    @Override
    public <T> Optional<T> find(String key, Class<T> tClass) {
        return Optional.ofNullable(data.get(key)).map(tClass::cast);
    }

    @Override
    public String toString() {
        return "DSItemImpl{" +
                "type=" + type +
                ", data=" + data +
                '}';
    }
}
