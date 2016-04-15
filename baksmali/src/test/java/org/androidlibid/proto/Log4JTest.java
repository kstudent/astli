package org.androidlibid.proto;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class Log4JTest {
   
    private final Logger logger1 = LogManager.getLogger("logger1");
    private final Logger logger2 = LogManager.getLogger("logger2");
    private final Logger setupLogger = LogManager.getLogger(org.androidlibid.proto.match.SetupLogger.class);
    
    @Test 
    public void doTest() {
        logger1.info("logger 1 info");
        logger1.debug("logger 1 debug");
        logger1.warn("logger 1 warn");
        logger2.info("logger 2 info");
        logger2.debug("logger 2 debug");
        logger2.warn("logger 2 warn");
        
        setupLogger.warn("setup warn");
        setupLogger.info("setup info");
        setupLogger.debug("setup debug");
        setupLogger.info("setup how di :)");
    }
    
}
