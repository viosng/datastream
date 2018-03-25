package com.spbsu.datastream.client;

import org.apache.maven.plugins.shade.DefaultShader;
import org.codehaus.plexus.logging.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by viosng on 17.03.2018.
 */
public class CustomShader extends DefaultShader {

    private static final PlexusLogger log = new PlexusLogger(LoggerFactory.getLogger(CustomShader.class));

    @Override
    protected Logger getLogger() {
        return log;
    }
}
