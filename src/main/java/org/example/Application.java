package org.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Application {

    private static final Logger LOGGER = LogManager.getLogger( Application.class );

    public static void main( String args[] ) {
        LOGGER.info( "Hello World." );
    }
}