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
class StatsToOrgLogger implements StatsLogger {

    private final static Logger LOGGER = LogManager.getLogger(StatsToOrgLogger.class);
    
    private final NumberFormat frmt = new DecimalFormat("#0.00");
        
    @Override
    public void logStats(Stats stats) {
        LOGGER.info("** Stats : ");
        
        Map<Integer, Integer> positions = stats.getPositions();
        Map<Classification, Integer> classifications = stats.getClassifications();
        int comparisons = stats.getComparisonCounter();
        
        LOGGER.info("*** Positions: ");
        for(Integer key : positions.keySet()) {
            LOGGER.info(" | {} | {} |", key.toString(), positions.get(key));
        }
        
        LOGGER.info("*** Classifications: ");
        
        LOGGER.info(" | state | amount |");
        for(Classification key : Classification.values()) {
            LOGGER.info(" | {} | {} |", 
                    key.toString(), 
                    classifications.get(key)
            );
        }
        
        int truePositives  = classifications.get(Classification.TPU);
        int falsePositives = classifications.get(Classification.FP);
        int totalPositives = truePositives + falsePositives;
        
        int trueNegatives  = classifications.get(Classification.TN);
        int falseNegatives = classifications.get(Classification.FN);
        int totalNegatives = trueNegatives + falseNegatives;
        int totals = totalPositives + totalNegatives;
        
        float fp_p = totalPositives > 0 ? ((float) falsePositives) * 100 / totalPositives : 0; 
        float fn_n = totalNegatives > 0 ? ((float) falseNegatives) * 100 / totalNegatives : 0;
        float fp =   totals > 0         ? ((float) falsePositives) * 100 / totals         : 0;
        float fn =   totals > 0         ? ((float) falseNegatives) * 100 / totals         : 0;
        
        LOGGER.info("| P(FP/P) | {}% |", frmt.format(fp_p));  
        LOGGER.info("| P(FN/N) | {}% |", frmt.format(fn_n));
        LOGGER.info("| P(FP)   | {}% |", frmt.format(fp));
        LOGGER.info("| P(FN)   | {}% |", frmt.format(fn));
        
        LOGGER.info("*** #comparisons : {}", comparisons);
        LOGGER.info("*** #db lookups: {}", stats.getDbLookups());
        LOGGER.info("*** Runtime: {} seconds", stats.getDiff() / 1000);
    }
}
