package org.androidlibid.proto.match;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;
import org.androidlibid.proto.match.Evaluation.Classification;
import org.androidlibid.proto.match.Evaluation.Position;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
class StatsLogger {

    private final static Logger LOGGER = LogManager.getLogger(StatsLogger.class);
    
    private final NumberFormat frmt = new DecimalFormat("#0.00");
        
    public void logStats(Map<Position, Integer> positions, Map<Classification, Integer> classifications, long diff) {
        
        LOGGER.info("* Stats: ");
        
        int total = 0;
        LOGGER.info("** Positions: ");
        for(Position key : Position.values()) {
            LOGGER.info(" | {} | {} |", key.toString(), positions.get(key));
            total += positions.get(key);
        }
        
        LOGGER.info("** Classifications: ");
        
        LOGGER.info(" | {} | {} | {} |", 
            "state", 
            "amount", 
            "%"
        );
        
        for(Classification key : Classification.values()) {
            LOGGER.info(" | {} | {} | {} |", 
                    key.toString(), 
                    classifications.get(key),
                    frmt.format((((float)classifications.get(key)) / total) * 100)
            );
        }
        
        int truePositives  = classifications.get(Classification.TRUE_POSITIVE);
        int falsePositives = classifications.get(Classification.FALSE_POSITIVE);
        int totalPositives = truePositives + falsePositives;
        
        int trueNegatives  = classifications.get(Classification.TRUE_NEGATIVE);
        int falseNegatives = classifications.get(Classification.FALSE_NEGATIVE);
        int totalNegatives = trueNegatives + falseNegatives;
        
        LOGGER.info("% of Positives which are false: {}%", 
                frmt.format(((float) falsePositives) * 100 / totalPositives)
        );  
        
        LOGGER.info("% of Negatives which are false: {}%", 
                frmt.format(((float) falseNegatives) * 100 / totalNegatives)
        );  
        
        
        
        LOGGER.info("* Runtime: {} seconds", diff / 1000);
    }
}
