package com.spbsu.datastream.core.util;

import java.util.UUID;

import com.google.common.base.Preconditions;

/**
 * @author Saveliev Nikolay
 * @date 29/03/2018
 */
public class ByteCodeBundleUtil {
    public final static String bundle = "bundle-";
    private ByteCodeBundleUtil() {}

    public static String bundlePrefix() {

        return String.format(bundle + "%s", UUID.randomUUID());
    }

    public static String extractBundlePrefix(String s) {
        Preconditions.checkArgument(s.startsWith(bundle));
        return s.substring(0, s.indexOf("."));
    }
}
