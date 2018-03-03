package com.spbsu.datastream.core.env;

import com.spbsu.datastream.core.DSPath;
import com.spbsu.datastream.core.data.DSItem;

import java.util.function.Consumer;

public interface DSEnv {
    void read(DSPath path, Consumer<DSItem> itemConsumer);
}
