package org.androidlibid.proto.match;

import java.sql.SQLException;
import java.util.Map;
import org.androidlibid.proto.Fingerprint;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public interface MatchingStrategy {
    public enum Status {
        OK,
        NOT_PERFECT,
        NO_MATCH_BY_NAME,
        NO_MATCH_BY_NAME_ALTHOUGH_IN_DB,
        NO_MATCH_BY_DISTANCE,
        CLASS_LENGTH_0;
    }
    
    public Map<Status, Integer> matchPrints(Map<String, Fingerprint> packagePrints) throws SQLException ;
}