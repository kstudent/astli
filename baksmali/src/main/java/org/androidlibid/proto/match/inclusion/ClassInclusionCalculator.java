package org.androidlibid.proto.match.inclusion;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.androidlibid.proto.Fingerprint;
import org.androidlibid.proto.match.FingerprintMatcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class ClassInclusionCalculator implements InclusionCalculator {

    private final FingerprintMatcher matcher;
    private final boolean allowRepeatedMatching;
    
    private boolean isLoggingActivated = false;
    private Set<String> interestedPackageNames = new HashSet<>();
    
    private static final Logger LOGGER = LogManager.getLogger(ClassInclusionCalculator.class);
    private final NumberFormat frmt = new DecimalFormat("#0.00");
    
    public ClassInclusionCalculator(FingerprintMatcher matcher, boolean allowRepeatedMatching) {
        this.matcher = matcher;
        this.allowRepeatedMatching = allowRepeatedMatching;
    }
    
    /**
     * Returns Inclusion Score, which indicates the amount of similar methods
     * both classes share.
     * 
     * example: score = 4.2 : there are 4 methods which match perfectly and 
     * one method which matches 20% OR there are 3 methods with .8 and so on...
     * 
     * @param subSet list of methods of the superset class
     * @param superSet list of methods of the subset class
     * @return score
     */
    @Override
    public double computeInclusion(List<Fingerprint> superSet, List<Fingerprint> subSet) {
        
        List<Fingerprint> superSetCopy = new LinkedList<>(superSet);
        
        if(subSet.isEmpty()) {
            return 0; 
        }
        
        setLoggingState(subSet);
        
        logClassAndMethodsHeader(superSet, subSet);
        
        double classScore = 0;
        
        for (Fingerprint element : subSet) {
            
            if(superSetCopy.isEmpty()) {
                break;
            }
            
            FingerprintMatcher.Result result = matcher.matchFingerprints(superSetCopy, element);
            
            String elementName = element.getName();
            
            double score = 0;
            double maxScore = element.getEuclideanLength();
            
            if(result.getMatchesByDistance().size() > 0) {
                Fingerprint closestElmentInBiggerSet = result.getMatchesByDistance().iterator().next();
                
                score = element.getSimilarityScoreToFingerprint(closestElmentInBiggerSet);
                
                String bestMatchName = closestElmentInBiggerSet.getName();
                
                logScore(elementName, bestMatchName, score, maxScore);
                    
                if(!allowRepeatedMatching) {
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
//        if(!LOGGER.isInfoEnabled() || superSet.isEmpty() || superSet.get(0) == null 
//            || subSet.isEmpty() || subSet.get(0) == null || !isLoggingActivated
//        ) {
//            return;
//        }
//            
//        MethodFingerprint superClass = superSet.get(0).getParent();
//        MethodFingerprint subClass = subSet.get(0).getParent();
//            
//        if(superClass == null || subClass == null) { 
//            return;
//        } 
//        
//        String superSetName = extractMethodName(superClass.getName());
//        String subSetName   = extractMethodName(subClass.getName());
//        LOGGER.info("**** {} (#Methods: {}, Length: {}) -> {} (#Methods: {}, Length: {}) ?", 
//            subSetName, 
//            subSet.size(),
//            frmt.format(subClass.getLength()),
//            superSetName, 
//            superSet.size(),
//            frmt.format(superClass.getLength())
//        ); 
        
    }

    private void logScore(String methodIdentifier, String bestMatchIdentifier, 
            double score, double maxScore) {
        
        if(methodIdentifier.isEmpty() || bestMatchIdentifier.isEmpty() || !isLoggingActivated) {
            return;
        }
        
        String methodName = extractMethodName(methodIdentifier);
        String bestMatchMethodName = extractMethodName(bestMatchIdentifier);
        
        boolean warn = !methodIdentifier.equals(bestMatchIdentifier);
        
        LOGGER.info("| {} | {} | {} | {} | {} |", 
                warn ? " X " : "",
                methodName,
                bestMatchMethodName, 
                frmt.format(score),
                frmt.format(maxScore)
        );
    }

    private void logClassScore(double classScore) {
        
        if(!isLoggingActivated) {
            return;
        }
        
        LOGGER.info("|  |  | {} |", frmt.format(classScore));
    }
    
    private String extractMethodName(String methodIdentifier) {
        
        if(methodIdentifier.contains(":")) {
            methodIdentifier = methodIdentifier.substring(methodIdentifier.indexOf(":") + 1);
        }
        if(methodIdentifier.contains(":")) {
            methodIdentifier = methodIdentifier.substring(methodIdentifier.indexOf(":") + 1);
        }
        
        return methodIdentifier; 
    }

    private void setLoggingState(List<Fingerprint> subSet) {
        if(!LOGGER.isInfoEnabled() || subSet.isEmpty() || subSet.get(0) == null) {
            return;
        }
        
        String pckgName = extractPackage(subSet.get(0).getName());
        
        isLoggingActivated = interestedPackageNames.contains(pckgName);
    }

    private String extractPackage(String identifier) {
        if(identifier.contains(":")) {
            identifier = identifier.substring(0, identifier.indexOf(":"));
        }
        
        return identifier;
    }

    public void setInterestedPackageNames(Set<String> interestedPackageNames) {
        this.interestedPackageNames = interestedPackageNames;
    }
}
