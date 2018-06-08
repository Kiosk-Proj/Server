package org.millburn.kiosk;

import org.millburn.kiosk.logging.Logger;
import org.millburn.kiosk.logging.LoggingHandler;

/**
 *
 * @author Javier
 */
public class ServerInitializer {
    private static Logger logger = new Logger();

    public static void main(String... args){
        LoggingHandler.initialize();
        logger.info("Initializing kiosk service...");


    }
}
