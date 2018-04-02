package com.spbsu.datastream.repo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractBundleUploadHandler implements BundleUploadHandler {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void onStart(String bundle) {
        log.info("onStart: {}", bundle);
    }

    @Override
    public void onError(String bundle, String message) {
        log.info("onError: {}, message: {}", bundle, message);
    }

    @Override
    public void onComplete(String bundle) {
        log.info("onComplete: {}", bundle);
    }
}
