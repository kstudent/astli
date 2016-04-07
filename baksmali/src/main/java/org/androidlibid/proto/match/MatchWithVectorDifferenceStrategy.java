package org.androidlibid.proto.match;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.androidlibid.proto.Fingerprint;
import org.androidlibid.proto.ao.FingerprintService;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class MatchWithVectorDifferenceStrategy implements MatchingStrategy {

    private final FingerprintService service;
    private final ResultEvaluator evaluator;
    private final FingerprintMatcher matcher;
    private final Level level;
    
    private static final Logger LOGGER = LogManager.getLogger(MatchWithVectorDifferenceStrategy.class);

    public MatchWithVectorDifferenceStrategy(FingerprintService service, 
            ResultEvaluator evaluator, FingerprintMatcher matcher, Level level) {
        this.service = service;
        this.evaluator = evaluator;
        this.matcher = matcher;
        this.level = level;
    }
    
    @Override
    public Map<Status, Integer> matchPrints(Map<String, Fingerprint> packagePrints) throws SQLException {
        
        Map<Status, Integer> stats = new HashMap<>();
        for(Status key : Status.values()) {
            stats.put(key, 0);
        }
        
        int count = 0;
        
        for(String packageName : packagePrints.keySet()) {
            
            Fingerprint packageNeedle = packagePrints.get(packageName);
            
            if(packageName.startsWith("android")) continue;
            if(packageName.equals("")) continue;
            
            LOGGER.info("{} ({}%)", packageName, ((float)(count++) / packagePrints.size()) * 100); 
         
            int packageDepth = StringUtils.countMatches(packageName, ".");
            
            for (Status result : matchNeedle(packageNeedle, packageDepth)) {
                stats.put(result, stats.get(result) + 1);
            }
        }
        
        return stats;
        
    }

    private List<Status> matchNeedle(Fingerprint packageNeedle, int packageDepth) throws SQLException {
        
        List<Status> stats = new ArrayList<>();
        List<Fingerprint> haystack; 
         
        switch(level) {
            case METHOD: 
                
                haystack = service.findMethodsByPackageDepth(packageDepth);
                
                for(Fingerprint classNeedle : packageNeedle.getChildFingerprints()) {
                    for(Fingerprint methodNeedle : classNeedle.getChildFingerprints()) {
                        FingerprintMatcher.Result result = matcher.matchFingerprints(haystack, methodNeedle);
                        stats.add(evaluator.evaluateResult(methodNeedle, result));
                    }
                }
                    
                break;
                
            case CLASS:
                haystack = service.findClassesByPackageDepth(packageDepth);
                
                for(Fingerprint classNeedle : packageNeedle.getChildFingerprints()) {
                    FingerprintMatcher.Result result = matcher.matchFingerprints(haystack, classNeedle);
                    stats.add(evaluator.evaluateResult(classNeedle, result));
                }
                    
                break;
                
            case PACKAGE:
                haystack = service.findPackagesByDepth(packageDepth);
                FingerprintMatcher.Result result = matcher.matchFingerprints(haystack, packageNeedle);
                stats.add(evaluator.evaluateResult(packageNeedle, result));
                break;
        }
        
        return stats;
    }
    
    public static enum Level {
        METHOD(),
        CLASS(),
        PACKAGE();
    }
    
}
