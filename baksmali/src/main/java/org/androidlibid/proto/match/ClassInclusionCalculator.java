package org.androidlibid.proto.match;

import java.util.LinkedList;
import java.util.List;
import org.androidlibid.proto.Fingerprint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class ClassInclusionCalculator {

    private final FingerprintMatcher matcher;
    
    private static final Logger LOGGER = LogManager.getLogger(ClassInclusionCalculator.class.getName());

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
     * @param subSet list of methods of the superset class
     * @param superSet list of methods of the subset class
     * @return score
     */
    public double computeClassInclusion(List<Fingerprint> superSet, List<Fingerprint> subSet) {
        
        
        List<Fingerprint> superSetCopy = new LinkedList<>(superSet);
        
        if(subSet.isEmpty()) {
            return 0; 
        }
        
        String superSetName = "";
        
        if(superSet.size()>0) {
            superSetName = superSet.get(0).getParent().getName();
        }
        
        LOGGER.info("**** {} ({}) -> {} ({}) ?", subSet.get(0).getParent().getName(), subSet.size(), superSetName, superSet.size()); 
        
        double classScore = 0;
        
        for (Fingerprint element : subSet) {
            
            if(superSetCopy.isEmpty()) {
                break;
            }
            
            FingerprintMatcher.Result result = matcher.matchFingerprints(superSetCopy, element);
            
            String elementName = element.getName();
            elementName = elementName.substring(elementName.indexOf(":"));
            
            double score = 0;
            
            if(result.getMatchesByDistance().size() > 0) {
                Fingerprint closestElmentInBiggerSet = result.getMatchesByDistance().get(0);
                
                score = element.computeSimilarityScore(closestElmentInBiggerSet);
                
                String bestMatchName = closestElmentInBiggerSet.getName();
                bestMatchName = bestMatchName.substring(bestMatchName.indexOf(":"));
                
                LOGGER.info("| {} | {} | {} |", new Object[]{elementName, bestMatchName, score});
                
                superSetCopy.remove(closestElmentInBiggerSet);    
            }
            
            classScore += score;
        }
        
        LOGGER.info("|  |  | {} |", classScore);
        
        if(Double.isNaN(classScore)) {
            throw new RuntimeException("What did you do this time?!");
        }
        
        return classScore;
    }
}
