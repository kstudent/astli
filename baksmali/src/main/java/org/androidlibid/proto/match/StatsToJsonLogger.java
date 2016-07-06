package org.androidlibid.proto.match;

import com.google.gson.GsonBuilder;
import org.androidlibid.proto.match.MatchingStrategy.Stats;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
class StatsToJsonLogger implements StatsLogger {

    private final static Logger LOGGER = LogManager.getLogger(StatsToJsonLogger.class);
    
    @Override
    public void logStats(Stats stats) {
        LOGGER.info(new GsonBuilder().create().toJson(stats));
    }
}
