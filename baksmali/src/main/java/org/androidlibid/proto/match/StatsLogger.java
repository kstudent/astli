package org.androidlibid.proto.match;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;
import org.androidlibid.proto.match.MatchingStrategy.Stats;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static org.androidlibid.proto.match.ResultEvaluator.*;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
class StatsLogger {

    private final static Logger LOGGER = LogManager.getLogger(StatsLogger.class);
    
    private final NumberFormat frmt = new DecimalFormat("#0.00");
        
    public void logStats(Stats stats, long diff) {
        Map<Integer, Integer> positions = stats.getPositions();
        Map<Classification, Integer> classifications = stats.getClassifications();
        int comparisons = stats.getComparisonCounter();
        
        LOGGER.info("** Positions: ");
        for(Integer key : positions.keySet()) {
            LOGGER.info(" | {} | {} |", key.toString(), positions.get(key));
        }
        
        LOGGER.info("** Classifications: ");
        
        LOGGER.info(" | state | amount |");
        for(Classification key : Classification.values()) {
            LOGGER.info(" | {} | {} |", 
                    key.toString(), 
                    classifications.get(key)
            );
        }
        
        int truePositives  = classifications.get(Classification.TP);
        int falsePositives = classifications.get(Classification.FP);
        int totalPositives = truePositives + falsePositives;
        
        int trueNegatives  = classifications.get(Classification.TN);
        int falseNegatives = classifications.get(Classification.FN);
        int totalNegatives = trueNegatives + falseNegatives;
        int totals = totalPositives + totalNegatives;
        
        float fp_p = ((float) falsePositives) * 100 / totalPositives; 
        float fn_n = ((float) falseNegatives) * 100 / totalNegatives;
        float fp =   ((float) falsePositives) * 100 / totals;
        float fn =   ((float) falseNegatives) * 100 / totals;
        
        LOGGER.info("| P(FP/P) | {}% |", frmt.format(fp_p));  
        LOGGER.info("| P(FN/N) | {}% |", frmt.format(fn_n));
        LOGGER.info("| P(FP)   | {}% |", frmt.format(fp));
        LOGGER.info("| P(FN)   | {}% |", frmt.format(fn));
        
        LOGGER.info("**   #comparisons : {}", comparisons);
        LOGGER.info("**   Runtime: {} seconds", diff / 1000);
    }
}
