package org.androidlibid.proto.match.inclusion;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;
import org.androidlibid.proto.Fingerprint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class PackageInclusionCalculator implements InclusionCalculator {
    
    private final InclusionCalculator calculator; 
    private final boolean allowRepeatedMatching;
    
    private final NumberFormat frmt = new DecimalFormat("#0.00");
    
    private static final Logger LOGGER = LogManager.getLogger(PackageInclusionCalculator.class.getName());

    public PackageInclusionCalculator(InclusionCalculator calculator, boolean allowRepeatedMatching) {
        this.calculator = calculator;
        this.allowRepeatedMatching = allowRepeatedMatching;
    }
    
    @Override
    public double computeInclusion(List<Fingerprint> superSet, List<Fingerprint> subSet) {
        
        logHeader();
        
        List<Fingerprint> superSetCopy = new LinkedList<>(superSet);
        
        if(subSet.isEmpty()) {
            return 0;
        }
        
        double packageScore = 0;

        for (Fingerprint clazz : subSet) {
            
            if (superSetCopy.isEmpty()) {
                break;
            }
            
            String clazzName = clazz.getName();
            
            logClassHeader(clazz);
            
            double maxScore = -1;
            Fingerprint maxScoreClazz = null;
            
            for (Fingerprint clazzCandidate : superSetCopy) {
                
                double score = calculator.computeInclusion(
                        clazzCandidate.getChildFingerprints(), clazz.getChildFingerprints());
                
                if(Double.isNaN(score) || score < 0) {
                    throw new RuntimeException("Like, srsly?");
                }
                
                if(score > maxScore) {
                    maxScoreClazz = clazzCandidate;
                    maxScore      = score;
                } 
            }
            
            if(maxScore == -1 || maxScoreClazz == null) {
                throw new RuntimeException("fix your code, maniac");
            }
            
            logResult(clazz, maxScoreClazz, maxScore);
            
            packageScore += maxScore;
            
            if(!allowRepeatedMatching) {
                if(!superSetCopy.remove(maxScoreClazz)) {
                    throw new RuntimeException("Tried to remove element"
                                + " that is not in the set.");
                }
            }
        }
        
        return packageScore;

    }

    private void logHeader() {
        LOGGER.info("| miss | class | matched | score |"); 
    }

    private void logClassHeader(Fingerprint clazz) {
        
        if(LOGGER.isDebugEnabled()) {
            String clazzName = clazz.getName();
            
            if(clazzName.contains(":")) {
                clazzName = clazzName.substring(clazzName.indexOf(":") + 1);
            }
            
            double perfectScore = calculator.computeInclusion(
                    clazz.getChildFingerprints(), 
                    clazz.getChildFingerprints()); 
            
            LOGGER.debug("*** myself: {}, which has {} methods and perfect score : {}.", 
                    clazzName, clazz.getChildFingerprints().size(), frmt.format(perfectScore)); 
        }
    }

    private void logResult(Fingerprint clazz, Fingerprint maxScoreClazz, 
            double maxScore) {
        
        String bestMatchName = maxScoreClazz.getName();
        if(bestMatchName.contains(":")) {
            bestMatchName = bestMatchName.substring(bestMatchName.indexOf(":") + 1);
        }

        String clazzName = clazz.getName();
        if(clazzName.contains(":")) {
            clazzName = clazzName.substring(clazzName.indexOf(":") + 1);
        }
        
        boolean warn = !clazzName.equals(bestMatchName); 
        
        LOGGER.info("| {} | {} | {} | {} |", 
                warn ? "X" : "",
                clazzName, bestMatchName, frmt.format(maxScore)); 
    }
}
