package com.spbsu.datastream.client.util;

import org.codehaus.plexus.logging.Logger;

/**
 * Created by viosng on 17.03.2018.
 */
public class PlexusLogger implements Logger {
    private final org.slf4j.Logger log;

    public PlexusLogger(org.slf4j.Logger log) {
        this.log = log;
    }

    @Override
    public void debug(String s) {
        log.debug(s);
    }

    @Override
    public void debug(String s, Throwable throwable) {
        log.debug(s, throwable);
    }

    @Override
    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    @Override
    public void info(String s) {
        log.info(s);
    }

    @Override
    public void info(String s, Throwable throwable) {
        log.info(s, throwable);
    }

    @Override
    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }

    @Override
    public void warn(String s) {
        log.warn(s);
    }

    @Override
    public void warn(String s, Throwable throwable) {
        log.warn(s, throwable);
    }

    @Override
    public boolean isWarnEnabled() {
        return log.isWarnEnabled();
    }

    @Override
    public void error(String s) {
        log.error(s);
    }

    @Override
    public void error(String s, Throwable throwable) {
        log.error(s, throwable);
    }

    @Override
    public boolean isErrorEnabled() {
        return log.isErrorEnabled();
    }

    @Override
    public void fatalError(String s) {
        error(s);
    }

    @Override
    public void fatalError(String s, Throwable throwable) {
        error(s, throwable);
    }

    @Override
    public boolean isFatalErrorEnabled() {
        return isErrorEnabled();
    }

    @Override
    public int getThreshold() {
        throw new UnsupportedOperationException("getThreshold");
    }

    @Override
    public void setThreshold(int i) {
        throw new UnsupportedOperationException("setThreshold");
    }

    @Override
    public Logger getChildLogger(String s) {
        throw new UnsupportedOperationException("getChildLogger");
    }

    @Override
    public String getName() {
        return log.getName();
    }
}
