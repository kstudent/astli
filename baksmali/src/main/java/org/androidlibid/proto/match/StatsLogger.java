package org.androidlibid.proto.match;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public interface StatsLogger {

    void logStats(MatchingStrategy.Stats stats);
    
}
