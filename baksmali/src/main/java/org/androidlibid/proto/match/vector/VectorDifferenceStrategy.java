package org.androidlibid.proto.match.vector;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.androidlibid.proto.Fingerprint;
import org.androidlibid.proto.ao.FingerprintService;
import org.androidlibid.proto.match.FingerprintMatcher;
import org.androidlibid.proto.match.FingerprintMatcher.Result;
import org.androidlibid.proto.match.MatchingStrategy;
import org.androidlibid.proto.match.ResultEvaluator;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class VectorDifferenceStrategy extends MatchingStrategy {

    private final FingerprintService service;
    private final ResultEvaluator evaluator;
    private final FingerprintMatcher matcher;
    private final Level level;
    
    private static final Logger LOGGER = LogManager.getLogger(VectorDifferenceStrategy.class);

    public VectorDifferenceStrategy(FingerprintService service, 
            ResultEvaluator evaluator, FingerprintMatcher matcher, Level level) {
        super();
        this.service = service;
        this.evaluator = evaluator;
        this.matcher = matcher;
        this.level = level;
    }

    @Override
    public void matchPrints(Map<String, Fingerprint> packagePrints) throws SQLException {
        
        evaluator.printResultRowHeader();
        
        int count = 0;
        
        for(String packageName : packagePrints.keySet()) {
            
            Fingerprint packageNeedle = packagePrints.get(packageName);
            
            if(packageName.startsWith("android")) continue;
            if(packageName.equals("")) continue;
            
            LOGGER.info("{} ({}%)", packageName, ((float)(count++) / packagePrints.size()) * 100); 
         
            int packageDepth = StringUtils.countMatches(packageName, ".");
            
            matchNeedle(packageNeedle, packageDepth);
        }
    }

    private void matchNeedle(Fingerprint packageNeedle, int packageDepth) throws SQLException {
        
        List<Fingerprint> haystack; 
         
//        switch(level) {
//            case METHOD: 
//                
//                haystack = service.findMethodsByPackageDepth(packageDepth);
//                
//                for(MethodFingerprint classNeedle : packageNeedle.getChildFingerprints()) {
//                    for(MethodFingerprint methodNeedle : classNeedle.getChildFingerprints()) {
//                        Result methodResult = matcher.matchFingerprints(haystack, methodNeedle);
//                        
//                        Result result = postProcessMethodResult(methodResult);
//                        
//                        incrementStats(evaluator.evaluateResult(result));
//                    }
//                }
//                    
//                break;
//                
//            case CLASS:
//                haystack = service.findClassesByPackageDepth(packageDepth);
//                
//                for(MethodFingerprint classNeedle : packageNeedle.getChildFingerprints()) {
//                    Result classResult = matcher.matchFingerprints(haystack, classNeedle);
//                    
//                    Result result = postProcessClassResult(classResult);
//                    
//                    incrementStats(evaluator.evaluateResult(result));
//                }
//                    
//                break;
//                
//            case PACKAGE:
//                haystack = service.findPackagesByDepth(packageDepth);
//                Result result = matcher.matchFingerprints(haystack, packageNeedle);
//                incrementStats(evaluator.evaluateResult(result));
//                break;
//        }
    }

    private Result postProcessClassResult(Result classResult) {
        
        Result result = new Result();
        
//        if(classResult.getMatchByName() != null) {
//            result.setMatchByName(classResult.getMatchByName().getParent());
//        }
//        
//        if(classResult.getNeedle() != null) {
//            result.setNeedle(classResult.getNeedle().getParent());
//        }
//        
//        Set<MethodFingerprint> matchesByDistance = new HashSet<>();
//        
//        for(MethodFingerprint matchByDistance : classResult.getMatchesByDistance()) {
//            matchesByDistance.add(matchByDistance.getParent());
//        }
//        
//        result.setMatchesByDistance(matchesByDistance);
        
        return result;
    }
    
    private Result postProcessMethodResult(Result methodResult) {
    
        Result result = new Result();
        
//        if(methodResult.getMatchByName() != null && methodResult.getMatchByName().getParent() != null) {
//            result.setMatchByName(methodResult.getMatchByName().getParent().getParent());
//        }
//        
//        if(methodResult.getNeedle() != null && methodResult.getNeedle().getParent() != null) {
//            result.setNeedle(methodResult.getNeedle().getParent().getParent());
//        }
//        
//        Set<MethodFingerprint> matchesByDistance = new HashSet<>();
//        
//        for(MethodFingerprint matchByDistance : methodResult.getMatchesByDistance()) {
//            if(matchByDistance.getParent() != null) {
//                matchesByDistance.add(matchByDistance.getParent());
//            }
//        }
        
//        result.setMatchesByDistance(matchesByDistance);
        
        return result;
    
    }
    
    public static enum Level {
        METHOD(),
        CLASS(),
        PACKAGE();
    }
    
}
