package com.spbsu.datastream.repo.service;

public interface BundleUploadHandler {
    void onStart(String bundle);
    void onError(String bundle, String message);
    void onComplete(String bundle);
}
