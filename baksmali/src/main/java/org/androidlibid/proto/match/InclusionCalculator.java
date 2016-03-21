package org.androidlibid.proto.match;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.androidlibid.proto.Fingerprint;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class InclusionCalculator {

    private final FingerprintMatcher matcher;
    private final double classMatchThreshold = 0.95d; 

    public InclusionCalculator(FingerprintMatcher matcher) {
        this.matcher = matcher;
    }
    
    /**
     * @param classSuperSet list of methods of the superset class
     * @param classSubSet   list of methods of the subset class
     * @return score
     */
    public double computeClassInclusion(List<Fingerprint> classSuperSet, List<Fingerprint> classSubSet) {
        
        if(classSubSet.isEmpty()) {
            return 0;
        }
        
        List<Fingerprint> smallerSet, biggerSet;
        
        if(classSuperSet.size() < classSubSet.size()) {
            smallerSet = classSuperSet;
            biggerSet  = new LinkedList<>(classSubSet);
        } else {
            smallerSet = classSubSet;
            biggerSet  = new LinkedList<>(classSuperSet);
        }
        
        double classScore = 0;
        
        for (Fingerprint element : smallerSet) {
            FingerprintMatcher.Result result = matcher.matchFingerprints(biggerSet, element);
            
            double score = 0;
            
            if(result.getMatchesByDistance().size() > 0) {
                Fingerprint closestElmentInBiggerSet = result.getMatchesByDistance().get(0);
                score = element.computeSimilarityScore(closestElmentInBiggerSet);
                biggerSet.remove(closestElmentInBiggerSet);    
            }
            
            classScore += score;
        }
        
        return classScore;
    }

    public double computePackageInclusion(List<Fingerprint> packageSuperSet, List<Fingerprint> packageSubSet) {
        if(packageSubSet.isEmpty()) {
            return 0;
        }
        
        double packageScore = 0;
        int amountMethods = 0;

        for (Iterator<Fingerprint> subSetIt = packageSubSet.iterator(); subSetIt.hasNext(); ) {
            Fingerprint classNeedle = subSetIt.next();
            
            amountMethods += classNeedle.getChildren().size();
            
            double maxClassScore = 0; 

            for (Iterator<Fingerprint> superSetIt = packageSuperSet.iterator(); superSetIt.hasNext(); ) {
                Fingerprint classCandidate = superSetIt.next();
            
                List<Fingerprint> methodSubSet   = new LinkedList<>(classNeedle.getChildren());
                List<Fingerprint> methodSuperSet = new LinkedList<>(classCandidate.getChildren());
                
                double classScore = computeClassInclusion(methodSuperSet, methodSubSet);
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
