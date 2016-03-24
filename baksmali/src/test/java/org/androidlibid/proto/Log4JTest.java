package org.androidlibid.proto;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class Log4JTest {
    
    @Test
    public void testLogger() {
        Logger logger = LogManager.getLogger(Log4JTest.class);
        logger.trace("trace");
        logger.info("info");
        logger.debug("debug");
        logger.warn("warn");
        logger.error("error");
        logger.fatal("fatal");
        logger.fatal("{}", 7);
        
    }    
    
}
