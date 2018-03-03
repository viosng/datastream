package com.spbsu.datastream.core.data;

import java.util.Optional;

public interface DSItem {
    DSType type();

    <T> T get(String key, Class<T> tClass);

    <T> Optional<T> find(String key, Class<T> tClass);
}
