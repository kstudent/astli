package org.androidlibid.proto.match;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nullable;
import org.androidlibid.proto.Fingerprint;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class MatchOnMethodLevelWithInclusionStrategy implements MatchingStrategy {

    private final FingerprintService service;
    private final InclusionCalculator calculator;
    private final ResultEvaluator evaluator; 
    private final double methodMatchThreshold  = 0.9999d;
    private final double packageMatchThreshold = 0.8d;
    private final double minimalMethodLengthForNeedleLookup = 12;

    public MatchOnMethodLevelWithInclusionStrategy(FingerprintService service, InclusionCalculator calculator, ResultEvaluator evaluator) {
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
            
            System.out.println(((float)(count++) / packagePrints.size()) * 100 + "%"); 
            
            if(packageNeedle.getName().startsWith("android")) continue;
            if(packageNeedle.getName().equals("")) continue;
         
            FingerprintMatcher.Result matches = findPackageInMethodHaystack(packageNeedle);

            MatchingStrategy.Status result = evaluator.evaluateResult(packageNeedle, matches);
            stats.put(result, stats.get(result) + 1);
        }
        
        return stats;
        
    }

    private @Nullable FingerprintMatcher.Result findPackageInMethodHaystack(Fingerprint packageNeedle) throws SQLException {
      
        FingerprintMatcher.Result result = new FingerprintMatcher.Result();
        result.setNeedle(packageNeedle);
        
        SortedMap<Double, Fingerprint> matchesByScore = new TreeMap<>();
        
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
                        for(Fingerprint alreadyScored : matchesByScore.values()) {
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
                        matchesByScore.put(packageScore * -1, packageCandidate);
                        
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
        
        result.setMatchesByDistance(new ArrayList<>(matchesByScore.values()));
        
        return result;
    }
}
