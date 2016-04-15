package org.androidlibid.proto.match;

import java.sql.SQLException;
import java.util.Map;
import org.androidlibid.proto.Fingerprint;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public interface MatchingStrategy {

    /**
    
    |-------------------+------------------------------------------------------|
    | OK                | if match by name was found and position = 1          |
    |-------------------+------------------------------------------------------|
    | NOT FIRST         | if match by name was found and position > 1          |
    |-------------------+------------------------------------------------------|
    | NOT IN CANDIDATES | if match by name was found, but not in candidate set |
    |-------------------+------------------------------------------------------|
    | NO MATCH BY NAME  | if match by name was not found. reasons:             |
    |                   | - lib of this package was not fingerprinted          |
    |                   | - package contains application code                  |
    |-------------------+------------------------------------------------------|
     
     */    
    public enum Status {
        OK,
        NOT_FIRST,
        NOT_IN_CANDIDATES,
        NO_MATCH_BY_NAME;
    }
    
    public Map<Status, Integer> matchPrints(Map<String, Fingerprint> packagePrints) throws SQLException ;
}
