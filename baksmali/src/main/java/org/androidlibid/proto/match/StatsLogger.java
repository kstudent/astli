package org.androidlibid.proto.match;

import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
class StatsLogger {

    private final static Logger LOGGER = LogManager.getLogger(StatsLogger.class);
    
    public void logStats(Map<MatchingStrategy.Status, Integer> stats) {
        
        LOGGER.info("* Stats: ");
        
        for(MatchingStrategy.Status key : MatchingStrategy.Status.values()) {
            LOGGER.info(" | {} | {} |", new Object[]{key.toString(), stats.get(key)});
        }
    }
}
