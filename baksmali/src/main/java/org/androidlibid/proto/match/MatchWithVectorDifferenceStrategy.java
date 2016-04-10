package org.androidlibid.proto.match;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
                        FingerprintMatcher.Result methodResult = matcher.matchFingerprints(haystack, methodNeedle);
                        
                        FingerprintMatcher.Result result = postProcessMethodResult(methodResult);
                        
                        stats.add(evaluator.evaluateResult(methodNeedle, result));
                    }
                }
                    
                break;
                
            case CLASS:
                haystack = service.findClassesByPackageDepth(packageDepth);
                
                for(Fingerprint classNeedle : packageNeedle.getChildFingerprints()) {
                    FingerprintMatcher.Result classResult = matcher.matchFingerprints(haystack, classNeedle);
                    
                    FingerprintMatcher.Result result = postProcessClassResult(classResult);
                    
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

    private FingerprintMatcher.Result postProcessClassResult(FingerprintMatcher.Result classResult) {
        
        FingerprintMatcher.Result result = new FingerprintMatcher.Result();
        
        if(classResult.getMatchByName() != null) {
            result.setMatchByName(classResult.getMatchByName().getParent());
        }
        
        if(classResult.getNeedle() != null) {
            result.setNeedle(classResult.getNeedle().getParent());
        }
        
        Set<Fingerprint> matchesByDistance = new HashSet<>();
        
        for(Fingerprint matchByDistance : classResult.getMatchesByDistance()) {
            matchesByDistance.add(matchByDistance.getParent());
        }
        
        result.setMatchesByDistance(matchesByDistance);
        
        return result;
    }
    
    private FingerprintMatcher.Result postProcessMethodResult(FingerprintMatcher.Result methodResult) {
    
        FingerprintMatcher.Result result = new FingerprintMatcher.Result();
        
        if(methodResult.getMatchByName() != null && methodResult.getMatchByName().getParent() != null) {
            result.setMatchByName(methodResult.getMatchByName().getParent().getParent());
        }
        
        if(methodResult.getNeedle() != null && methodResult.getNeedle().getParent() != null) {
            result.setNeedle(methodResult.getNeedle().getParent().getParent());
        }
        
        Set<Fingerprint> matchesByDistance = new HashSet<>();
        
        for(Fingerprint matchByDistance : methodResult.getMatchesByDistance()) {
            if(matchByDistance.getParent() != null) {
                matchesByDistance.add(matchByDistance.getParent());
            }
        }
        
        result.setMatchesByDistance(matchesByDistance);
        
        return result;
    
    }
    
    public static enum Level {
        METHOD(),
        CLASS(),
        PACKAGE();
    }
    
}
