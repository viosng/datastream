package com.spbsu.datastream.core.operation;

import com.spbsu.datastream.core.data.DSItem;

import java.util.function.BiPredicate;
import java.util.function.IntFunction;

public interface GroupKey {

    IntFunction<DSItem> hash();

    BiPredicate<DSItem, DSItem> eq();
}
