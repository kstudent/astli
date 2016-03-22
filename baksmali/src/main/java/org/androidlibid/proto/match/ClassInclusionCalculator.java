package org.androidlibid.proto.match;

import java.util.LinkedList;
import java.util.List;
import org.androidlibid.proto.Fingerprint;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class ClassInclusionCalculator {

    private final FingerprintMatcher matcher;

    public ClassInclusionCalculator(FingerprintMatcher matcher) {
        this.matcher = matcher;
    }
    
    /**
     * Returns Inclusion Score, which indicates the amount of similar methdos
     * both classes share.
     * 
     * example: score = 4.2 : there are 4 methods which match perfectly and 
     * one method which matches 20% OR there are 3 methods with .8 and so on...
     * 
     * @param methodsOfclassA list of methods of the superset class
     * @param methodsOfClassB list of methods of the subset class
     * @return score
     */
    public double computeClassInclusion(List<Fingerprint> methodsOfclassA, List<Fingerprint> methodsOfClassB) {
        
        List<Fingerprint> smallerSet, biggerSet;
        
        if(methodsOfclassA.size() < methodsOfClassB.size()) {
            smallerSet = methodsOfclassA;
            biggerSet  = new LinkedList<>(methodsOfClassB);
        } else {
            smallerSet = methodsOfClassB;
            biggerSet  = new LinkedList<>(methodsOfclassA);
        }
        
        if(smallerSet.isEmpty()) {
            return 0; 
        }
        
        double classScore = 0;
        double normalizer = 0;
        
        for (Fingerprint element : smallerSet) {
            FingerprintMatcher.Result result = matcher.matchFingerprints(biggerSet, element);
            
            double score = 0;
            
            if(result.getMatchesByDistance().size() > 0) {
                Fingerprint closestElmentInBiggerSet = result.getMatchesByDistance().get(0);
                
                double length = Math.max(element.euclideanNorm(), closestElmentInBiggerSet.euclideanNorm());
                normalizer += length;
                score = element.computeSimilarityScore(closestElmentInBiggerSet) * length;
                
                biggerSet.remove(closestElmentInBiggerSet);    
            }
            
            classScore += score;
        }
        
        double val = (classScore / normalizer) * smallerSet.size();
        
        if(Double.isNaN(val)) {
            throw new RuntimeException("What did you do this time?!");
        }
        
        return val;
    }
}
