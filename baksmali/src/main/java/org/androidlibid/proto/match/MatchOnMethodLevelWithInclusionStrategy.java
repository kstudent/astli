package org.androidlibid.proto.match;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
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
    private final FingerprintMatcher matcher;
    private final ResultEvaluator evaluator; 
    private final double methodMatchThreshold  = 0.9999d;
    private final double classMatchThreshold   = 0.95d;
    private final double packageMatchThreshold = 0.8d;
    private final double orbitBreadth          = 0.2d;

    public MatchOnMethodLevelWithInclusionStrategy(FingerprintService service, FingerprintMatcher matcher, ResultEvaluator evaluator) {
        this.service = service;
        this.matcher = matcher;
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
        
        SortedMap<Double, Fingerprint> matchesByScore = new TreeMap<>();
        
        boolean breakOut = false; 
        
        for(Fingerprint classNeedle : packageNeedle.getChildren()) {
            for(Fingerprint methodNeedle : classNeedle.getChildren()) {
                double length = methodNeedle.euclideanNorm();
                double size   = length * orbitBreadth;
                System.out.println("..." + methodNeedle.getName() + " (" + length + ")"); 
                
                if(length < 10) {
                    break;
                }
                
                List<Fingerprint> methodHaystack = service.findMethodsByLength(length, size);                
                System.out.println("   " + methodHaystack.size() + " needles to check..."); 
                
                for(Fingerprint methodCandidate : methodHaystack) {
                    
                    double methodDiff = methodCandidate.computeSimilarityScore(methodNeedle);
                    
                    if(methodDiff > methodMatchThreshold) {
                        
                        Fingerprint packageCandidate = service.getPackageHierarchyByMethod(methodCandidate);

                        List<Fingerprint> classSuperSet = new LinkedList<>(packageCandidate.getChildren());
                        List<Fingerprint> classSubSet   = new LinkedList<>(packageNeedle.getChildren());

                        double packageScore = checkPackageInclusion(classSuperSet, classSubSet);
                        
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

    private double checkClassInclusion(List<Fingerprint> superSet, List<Fingerprint> subSet) {
        
        if(subSet.isEmpty()) {
            return 0;
        }
        
        double classScore = 0;
        
        //TODO: exclude found methods!
        
        for (Fingerprint element : subSet) {
            FingerprintMatcher.Result result = matcher.matchFingerprints(superSet, element);
            
            if(result.getMatchesByDistance().size() > 0) {
                Fingerprint closestElmentInSuperSet = result.getMatchesByDistance().get(0);
                double diff   = element.euclideanDiff(closestElmentInSuperSet);
                double length = element.euclideanNorm();
                double score  = 1 - (diff / length);
                if(score < 0) score = 0;
                classScore += score;
            }
        }
        
        return classScore; 
    }

    private double checkPackageInclusion(List<Fingerprint> superSet, List<Fingerprint> subSet) {
        if(subSet.isEmpty()) {
            return 0;
        }
        
        double packageScore = 0;
        int amountMethods = 0;

        for (Iterator<Fingerprint> subSetIt = subSet.iterator(); subSetIt.hasNext(); ) {
            Fingerprint classNeedle = subSetIt.next();
            
            amountMethods += classNeedle.getChildren().size();
            
            double maxClassScore = 0; 

            for (Iterator<Fingerprint> superSetIt = superSet.iterator(); superSetIt.hasNext(); ) {
                Fingerprint classCandidate = superSetIt.next();
            
                List<Fingerprint> methodSubSet   = new LinkedList<>(classNeedle.getChildren());
                List<Fingerprint> methodSuperSet = new LinkedList<>(classCandidate.getChildren());
                
                double classScore = checkClassInclusion(methodSuperSet, methodSubSet);
                double classScoreNormalizied = classScore / methodSubSet.size(); 
                
                maxClassScore = (classScore > maxClassScore) ? classScore : maxClassScore; 
                
                if(classScoreNormalizied > classMatchThreshold) {
                    superSetIt.remove();
                    break;
                }
            }
            
            packageScore += maxClassScore;
            
        }
        
        return (packageScore / amountMethods); 
    }

}
