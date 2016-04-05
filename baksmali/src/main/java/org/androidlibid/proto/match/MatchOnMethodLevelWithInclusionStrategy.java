package org.androidlibid.proto.match;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.androidlibid.proto.Fingerprint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class MatchOnMethodLevelWithInclusionStrategy implements MatchingStrategy {

    private final FingerprintService service;
    private final PackageInclusionCalculator calculator;
    private final ResultEvaluator evaluator; 
    private final double methodMatchThreshold  = 0.9999d;
    private final double packageMatchThreshold = 0.8d;
    private final double minimalMethodLengthForNeedleLookup = 12;
    
    private static final Logger LOGGER = LogManager.getLogger( MatchOnMethodLevelWithInclusionStrategy.class.getName());

    public MatchOnMethodLevelWithInclusionStrategy(FingerprintService service, PackageInclusionCalculator calculator, ResultEvaluator evaluator) {
        this.service = service;
        this.calculator = calculator;
        this.evaluator = evaluator;
    }

    @Override
    public Map<Status, Integer> matchPrints(Map<String, Fingerprint> packagePrints) throws SQLException {
        Map<Status, Integer> stats = new HashMap<>();
        for(Status key : Status.values()) {
            stats.put(key, 0);
        }
        
        int count = 0;
        
        for(Fingerprint packageNeedle : packagePrints.values()) {
            
            LOGGER.info("{}%", ((float)(count++) / packagePrints.size()) * 100); 
            
            if(packageNeedle.getName().startsWith("android")) continue;
            if(packageNeedle.getName().equals("")) continue;
            
            FingerprintMatcher.Result matches = findPackageInMethodHaystack(packageNeedle);
//            FingerprintMatcher.Result matches = findPackage(packageNeedle);

            MatchingStrategy.Status result = evaluator.evaluateResult(packageNeedle, matches);
            stats.put(result, stats.get(result) + 1);
        }
        
        return stats;
        
    }
    
    private @Nullable FingerprintMatcher.Result findPackage(Fingerprint packageNeedle) throws SQLException {
       
        
        FingerprintMatcher.Result result = new FingerprintMatcher.Result();
        result.setNeedle(packageNeedle);
        
        List<Fingerprint> matchesByScore = new ArrayList<>();
        LOGGER.info("* {}", packageNeedle.getName());
        
        LOGGER.info("** match with myself");
        
        double perfectScore = calculator.computePackageInclusion(packageNeedle.getChildFingerprints(), packageNeedle.getChildFingerprints());
        packageNeedle.setInclusionScore(perfectScore);

        LOGGER.info("** perfect score was {}", perfectScore);
        
        for(Fingerprint packageCandidate : service.findPackages()) {
            
            LOGGER.debug("** {}", packageCandidate.getName());
            
            Fingerprint packageHierarchy = service.getPackageHierarchy(packageCandidate);
            
            double packageScore = calculator.computePackageInclusion(packageHierarchy.getChildFingerprints(), packageNeedle.getChildFingerprints());
                        
            packageHierarchy.setInclusionScore(packageScore);
            
            if(packageHierarchy.getName().equals(packageNeedle.getName())) {
                result.setMatchByName(packageHierarchy);
            }
            
            matchesByScore.add(packageHierarchy);
            
            LOGGER.debug("** match: {} score: {}", packageHierarchy.getName() , packageScore);
            
        }
                
        Collections.sort(matchesByScore, new SortDescByInclusionScoreComparator());
        
        result.setMatchesByDistance(matchesByScore);
        
        return result;
    
    }

    private @Nullable FingerprintMatcher.Result findPackageInMethodHaystack(Fingerprint packageNeedle) throws SQLException {
      
        FingerprintMatcher.Result result = new FingerprintMatcher.Result();
        result.setNeedle(packageNeedle);
        
        double perfectScore = calculator.computePackageInclusion(packageNeedle.getChildFingerprints(), packageNeedle.getChildFingerprints());
        packageNeedle.setInclusionScore(perfectScore);
        
        List<Fingerprint> matchesByScore = new ArrayList<>();
        
        boolean breakOut = false; 
        
        LOGGER.info("* {} ({})", packageNeedle.getName(), perfectScore); 
        
        for(Fingerprint classNeedle : packageNeedle.getChildFingerprints()) {
            for(Fingerprint methodNeedle : classNeedle.getChildFingerprints()) {
                double length = methodNeedle.getLength();
                double size   = length * (1 - methodMatchThreshold);
                
                if(length < minimalMethodLengthForNeedleLookup) {
                    break;
                }
                
                LOGGER.info("** {} ({})", methodNeedle.getName(), length); 
                
                List<Fingerprint> methodHaystack = service.findMethodsByLength(length, size);                
                
                LOGGER.info("{} needles to check", methodHaystack.size()); 
                
                for(Fingerprint methodCandidate : methodHaystack) {
                    
                    double methodSimilarityScore = methodCandidate.getSimilarityScoreToFingerprint(methodNeedle);
                    double maxLength = Math.max(methodNeedle.getLength(), methodCandidate.getLength());
                    
                    if((methodSimilarityScore / maxLength) > methodMatchThreshold) {
                        
                        Fingerprint packageCandidate = service.getPackageHierarchyByMethod(methodCandidate);
                        
                        boolean continueFlag = false;
                        
                        for(Fingerprint alreadyScored : matchesByScore) {
                            if(alreadyScored.getName().equals(packageCandidate.getName())) {
                                continueFlag = true;
                                break;
                            }
                        }
                        if (continueFlag) continue;

                        List<Fingerprint> classSuperSet = new LinkedList<>(packageCandidate.getChildFingerprints());
                        List<Fingerprint> classSubSet   = new LinkedList<>(packageNeedle.getChildFingerprints());

                        double packageScore = calculator.computePackageInclusion(classSuperSet, classSubSet);
                        
                        packageCandidate.setInclusionScore(packageScore);
                        
                        //TODO: find meaningful threshold (evaluation?) 
                        matchesByScore.add(packageCandidate);
                        
                        if(packageCandidate.getName().equals(packageNeedle.getName())) {
                            result.setMatchByName(packageCandidate);
                        }
                        
                        LOGGER.info("{} -> {} (sim: {} {})", packageNeedle.getName(), packageCandidate.getName(), packageScore, packageScore / perfectScore);
                        
                        if((packageScore / perfectScore) > packageMatchThreshold) {
                            breakOut = true; 
                            break; 
                        }
                    }
                }
                
                if(breakOut) {
                    LOGGER.info("found breakout");
                    break;
                } else {
                    LOGGER.info("continue new method :(");
                }
            }
            
            if(breakOut) {
                break;
            } 
        }    
        
        Collections.sort(matchesByScore, new SortDescByInclusionScoreComparator());

        result.setMatchesByDistance(matchesByScore);
        
        if(result.getMatchByName() == null) {
            
            LOGGER.info("Did not find match by name for {}", packageNeedle.getName());
            
            List<Fingerprint> packagesWithSameName = service.findPackageByName(packageNeedle.getName());
            
            if(!packagesWithSameName.isEmpty()) {
                Fingerprint matchByName = service.getPackageHierarchy(packagesWithSameName.get(0));
                
                List<Fingerprint> classSubSet   = new LinkedList<>(packageNeedle.getChildFingerprints());
                List<Fingerprint> classSuperSet = new LinkedList<>(matchByName.getChildFingerprints());
                
                double score = calculator.computePackageInclusion(classSuperSet, classSubSet);
                matchByName.setInclusionScore(score);
                result.setMatchByName(matchByName);
            }
        }
        
        return result;
    }
    
    private class SortDescByInclusionScoreComparator implements Comparator<Fingerprint> {
        @Override
        public int compare(Fingerprint that, Fingerprint other) {
            double scoreNeedleThat  = that.getInclusionScore();
            double scoreNeedleOther = other.getInclusionScore();
            if (scoreNeedleThat > scoreNeedleOther) return -1;
            if (scoreNeedleThat < scoreNeedleOther) return  1;
            return 0;
        }
    }
}
