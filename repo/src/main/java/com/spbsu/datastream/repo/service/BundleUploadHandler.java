package com.spbsu.datastream.repo.service;

public interface BundleUploadHandler {
    default void onStart(String bundle){}
    default void onError(String bundle, String message){}
    default void onComplete(String bundle){}
}
