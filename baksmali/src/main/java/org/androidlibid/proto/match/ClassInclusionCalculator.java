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
    private final boolean disableRepeatedMatches;
    
    private static final Logger LOGGER = LogManager.getLogger(ClassInclusionCalculator.class.getName());

    public ClassInclusionCalculator(FingerprintMatcher matcher, boolean disableRepeatedMatches) {
        this.matcher = matcher;
        this.disableRepeatedMatches = disableRepeatedMatches;
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
        
        logClassAndMethodsHeader(superSet, subSet);
        
        double classScore = 0;
        
        for (Fingerprint element : subSet) {
            
            if(superSetCopy.isEmpty()) {
                break;
            }
            
            FingerprintMatcher.Result result = matcher.matchFingerprints(superSetCopy, element);
            
            String elementName = element.getName();
            
            double score = 0;
            double maxScore = element.getLength();
            
            if(result.getMatchesByDistance().size() > 0) {
                Fingerprint closestElmentInBiggerSet = result.getMatchesByDistance().iterator().next();
                
                score = element.getSimilarityScoreToFingerprint(closestElmentInBiggerSet);
                
                String bestMatchName = closestElmentInBiggerSet.getName();
                
                logScore(elementName, bestMatchName, score, maxScore);
                    
                if(disableRepeatedMatches) {
                    if(!superSetCopy.remove(closestElmentInBiggerSet)) {
                        throw new RuntimeException("Tried to remove element"
                                + " that is not in the set.");
                    }    
                }
            }
            
            classScore += score;
        }
        
        logClassScore(classScore);
        
        if(Double.isNaN(classScore)) {
            throw new RuntimeException("What did you do this time?!");
        }
        
        return classScore;
    }

    private void logClassAndMethodsHeader(List<Fingerprint> superSet, List<Fingerprint> subSet) {
        if(!LOGGER.isInfoEnabled() || superSet.isEmpty() || superSet.get(0) == null 
            || subSet.isEmpty() || subSet.get(0) == null
        ) {
            return;
        }
            
        Fingerprint superClass = superSet.get(0).getParent();
        Fingerprint subClass = subSet.get(0).getParent();
            
        if(superClass == null || subClass == null) { 
            return;
        } 
        
        String superSetName = extractClassName(superClass.getName());
        String subSetName   = extractClassName(subClass.getName());
        LOGGER.info("**** {} (#Methods: {}, Length: {}) -> {} (#Methods: {}, Length: {}) ?", 
            subSetName, 
            subSet.size(),
            subClass.getLength(),
            superSetName, 
            superSet.size(),
            superClass.getLength()
        ); 
        
    }

    private void logScore(String elementName, String bestMatchName, double score, double maxScore) {
        
        if(!LOGGER.isInfoEnabled() || elementName.isEmpty() || bestMatchName.isEmpty() ) {
            return;
        }
        
        LOGGER.info("| {} | {} | {} | {} |", 
                extractClassName(elementName), 
                extractClassName(bestMatchName), 
                score,
                maxScore
        );
        
    }

    private void logClassScore(double classScore) {
        LOGGER.info("|  |  | {} |", classScore);
    }
    
    private String extractClassName(String methodIdentifier) {
        
        if(methodIdentifier.contains(":")) {
            methodIdentifier = methodIdentifier.substring(methodIdentifier.indexOf(":") + 1);
        }
        if(methodIdentifier.contains(":")) {
            methodIdentifier = methodIdentifier.substring(methodIdentifier.indexOf(":") + 1);
        }
        if(methodIdentifier.contains("(")) {
            methodIdentifier = methodIdentifier.substring(0, methodIdentifier.indexOf("("));
        }
        
        return methodIdentifier; 
        
    }
}
