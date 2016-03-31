package org.androidlibid.proto.match;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
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
            
//            FingerprintMatcher.Result matches = findPackageInMethodHaystack(packageNeedle);
            FingerprintMatcher.Result matches = findPackage(packageNeedle);

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
        
        double perfectScore = calculator.computePackageInclusion(packageNeedle.getChildren(), packageNeedle.getChildren());
        packageNeedle.setInclusionScore(perfectScore);

        LOGGER.info("** perfect score was {}", perfectScore);
        
        for(Fingerprint packageCandidate : service.findPackages()) {
            
            LOGGER.debug("** {}", packageCandidate.getName());
            
            Fingerprint packageHierarchy = service.getPackageHierarchy(packageCandidate);
            
            double packageScore = calculator.computePackageInclusion(packageHierarchy.getChildren(), packageNeedle.getChildren());
                        
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
        
        List<Fingerprint> matchesByScore = new ArrayList<>();
        
        boolean breakOut = false; 
        
        for(Fingerprint classNeedle : packageNeedle.getChildren()) {
            for(Fingerprint methodNeedle : classNeedle.getChildren()) {
                double length = methodNeedle.euclideanNorm();
                double size   = length * (1 - methodMatchThreshold);
                
                if(length < minimalMethodLengthForNeedleLookup) {
                    break;
                }
                
                System.out.println("..." + methodNeedle.getName() + " (" + length + ")"); 
                
                List<Fingerprint> methodHaystack = service.findMethodsByLength(length, size);                
                System.out.println("   " + methodHaystack.size() + " needles to check..."); 
                
                for(Fingerprint methodCandidate : methodHaystack) {
                    
                    double methodDiff = methodCandidate.computeSimilarityScore(methodNeedle);
                    
                    if(methodDiff > methodMatchThreshold) {
                        
                        Fingerprint packageCandidate = service.getPackageHierarchyByMethod(methodCandidate);
                        
                        boolean continueFlag = false;
                        for(Fingerprint alreadyScored : matchesByScore) {
                            if(alreadyScored.getName().equals(packageCandidate.getName())) {
                                System.out.println("   " + packageCandidate.getName() + " already in score table. next needle, please.");
                                continueFlag = true;
                                break;
                            }
                        }
                        if (continueFlag) continue;

                        List<Fingerprint> classSuperSet = new LinkedList<>(packageCandidate.getChildren());
                        List<Fingerprint> classSubSet   = new LinkedList<>(packageNeedle.getChildren());

                        double packageScore = calculator.computePackageInclusion(classSuperSet, classSubSet);
                        
                        packageCandidate.setInclusionScore(packageScore);
                        
                        //TODO: find meaningful threshold (evaluation?) 
                        matchesByScore.add(packageCandidate);
                        
                        if(packageCandidate.getName().equals(packageNeedle.getName())) {
                            result.setMatchByName(packageCandidate);
                        }
                        
                        System.out.println("   " + packageNeedle.getName() + " - " + packageCandidate.getName() + " sim: " + packageScore);
                        
                        if(packageScore > packageMatchThreshold) {
                            breakOut = true; 
                            break; 
                        }
                    }
                }
                
                if(breakOut) {
                    System.out.println("   found breakout");
                    break;
                } else {
                    System.out.println("   continue new method :(");
                }
            }
            
            if(breakOut) {
                break;
            } 
        }    
        
        Collections.sort(matchesByScore, new SortDescByInclusionScoreComparator());

        result.setMatchesByDistance(matchesByScore);
        
        if(result.getMatchByName() == null) {
            List<Fingerprint> packagesWithSameName = service.findPackageByName(packageNeedle.getName());
            
            if(!packagesWithSameName.isEmpty()) {
                Fingerprint matchByName = service.getPackageHierarchy(packagesWithSameName.get(0));
                
                List<Fingerprint> classSubSet   = new LinkedList<>(packageNeedle.getChildren());
                List<Fingerprint> classSuperSet = new LinkedList<>(matchByName.getChildren());
                
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
