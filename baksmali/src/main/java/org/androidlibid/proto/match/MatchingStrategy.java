package org.androidlibid.proto.match;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.androidlibid.proto.PackageHierarchy;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public abstract class MatchingStrategy {

    private final Map<Evaluation.Classification, Integer> classifications;
    private final Map<Evaluation.Position, Integer> positions;
    
    public Map<Evaluation.Classification, Integer> getClassifications() {
        return classifications;
    }

    public Map<Evaluation.Position, Integer> getPositions() {
        return positions;
    }

    public MatchingStrategy() {
        classifications = new HashMap<>();
        for(Evaluation.Classification key : Evaluation.Classification.values()) {
            classifications.put(key, 0);
        }
        
        positions = new HashMap<>();
        for(Evaluation.Position key : Evaluation.Position.values()) {
            positions.put(key, 0);
        }
    }
    
    protected void incrementStats(Evaluation evaluation) {
        positions.put(evaluation.getPosition(), positions.get(evaluation.getPosition()) + 1);
        classifications.put(evaluation.getClassification(), classifications.get(evaluation.getClassification()) + 1);
    } 
    
    abstract public void matchHierarchies(Map<String, PackageHierarchy> hierarchies) 
            throws SQLException;

}
