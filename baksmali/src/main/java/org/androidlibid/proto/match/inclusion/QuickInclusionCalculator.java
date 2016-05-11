package org.androidlibid.proto.match.inclusion;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.androidlibid.proto.Fingerprint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class QuickInclusionCalculator implements InclusionCalculator {
    
    private final NumberFormat frmt = new DecimalFormat("#0.00");
    private static final Logger LOGGER = LogManager.getLogger(QuickInclusionCalculator.class.getName());

    double methodSimThresold = .9d;

    public QuickInclusionCalculator() {
    }
    
    @Override
    public double computeInclusion(List<Fingerprint> classCandidates, List<Fingerprint> subSet) {
        
        logHeader();
        
        List<Fingerprint> classCandidatesCopy = new LinkedList<>(classCandidates);
        
        if(subSet.isEmpty()) {
            return 0;
        }
        
        double totalScore = 0; 
        
        for(Fingerprint classNeedle : subSet) {
            
            Fingerprint methodNeedle = classNeedle.getChildFingerprints().get(0);
            
            Fingerprint classCandiate;
            
            do {
                classCandiate = findClassCandidateByNeedle(methodNeedle, classCandidatesCopy);

                if(classCandiate == null) {
                    break;
                }

            } while (!isClassIncluded(classNeedle, classCandiate));
            
            if(classCandiate == null) {
                logResult(classNeedle, classCandiate, 0);
            } else {
                double score = classNeedle.getLength(); 
                logResult(classNeedle, classCandiate, score);
                totalScore += score;
                classCandidatesCopy.remove(classCandiate);
            }
        }
        
        return totalScore;

    }

    private boolean isClassIncluded(Fingerprint classNeedle, Fingerprint classCandidate) {
        
        List<Fingerprint> methodCandidates = new LinkedList<>(classCandidate.getChildFingerprints());
        
        for(Fingerprint methodNeedle : classNeedle.getChildFingerprints()) {
            
            boolean foundSimilarMethod = false; 
            
            for(Iterator<Fingerprint> it = methodCandidates.iterator(); it.hasNext();) {
                Fingerprint methodCandidate = it.next();
                
                double sim = methodNeedle.getNonCommutativeSimilarityScoreToFingerprint(methodCandidate);
                double simN = sim / methodNeedle.getLength();
                
                if(simN > methodSimThresold) {
                    foundSimilarMethod = true;
                    it.remove();
                    break;
                }
            }
            
            if(!foundSimilarMethod) {
                return false;
            }
        }
        
        return true;
        
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
            
//            double perfectScore = calculator.computeInclusion(
//                    clazz.getChildFingerprints(), 
//                    clazz.getChildFingerprints()); 
//            
//            LOGGER.debug("*** myself: {}, which has {} methods and perfect score : {}.", 
//                    clazzName, clazz.getChildFingerprints().size(), frmt.format(perfectScore)); 
        }
    }

    private void logResult(Fingerprint classNeedle, Fingerprint classBestMatch, 
            double maxScore) {
        
        String bestMatchName = "<none>";
        
        if(classBestMatch != null) {
            bestMatchName = classBestMatch.getName();
            if(bestMatchName.contains(":")) {
                bestMatchName = bestMatchName.substring(bestMatchName.indexOf(":") + 1);
            }
        }

        String clazzName = classNeedle.getName();
        if(clazzName.contains(":")) {
            clazzName = clazzName.substring(clazzName.indexOf(":") + 1);
        }
        
        boolean warn = !clazzName.equals(bestMatchName); 
        
        LOGGER.info("| {} | {} | {} | {} |", 
                warn ? "X" : "",
                clazzName, bestMatchName, frmt.format(maxScore)); 
    }

    private Fingerprint findClassCandidateByNeedle(Fingerprint methodNeedle, List<Fingerprint> classCandidates) {
        
        for(Fingerprint classCandidate : classCandidates) {
            
            for(Fingerprint methodCandidate : classCandidate.getChildFingerprints()) {
                
                if(methodCandidate.getName().equals(methodNeedle.getName())) {
                    debugDiff(methodNeedle, methodCandidate);
                }
                
                double sim = methodNeedle.getNonCommutativeSimilarityScoreToFingerprint(methodCandidate);
                double simN = sim / methodNeedle.getLength();

                if(simN > methodSimThresold) {
                    return classCandidate;
                }
            }
        }
        
        return null;
    }

    private void debugDiff(Fingerprint needle, Fingerprint candidate) {
        
    }
    
}

//        double packageScore = 0;
//
//        for (Fingerprint clazz : subSet) {
//            
//            if (superSetCopy.isEmpty()) {
//                break;
//            }
//            
//            String clazzName = clazz.getName();
//            
//            logClassHeader(clazz);
//            
//            double maxScore = -1;
//            Fingerprint maxScoreClazz = null;
//            
//            for (Fingerprint clazzCandidate : superSetCopy) {
//                
//                double score = calculator.computeInclusion(
//                        clazzCandidate.getChildFingerprints(), clazz.getChildFingerprints());
//                
//                if(Double.isNaN(score) || score < 0) {
//                    throw new RuntimeException("Like, srsly?");
//                }
//                
//                if(score > maxScore) {
//                    maxScoreClazz = clazzCandidate;
//                    maxScore      = score;
//                } 
//            }
//            
//            if(maxScore == -1 || maxScoreClazz == null) {
//                throw new RuntimeException("fix your code, maniac");
//            }
//            
//            logResult(clazz, maxScoreClazz, maxScore);
//            
//            packageScore += maxScore;
//            
//            if(!allowRepeatedMatching) {
//                if(!superSetCopy.remove(maxScoreClazz)) {
//                    throw new RuntimeException("Tried to remove element"
//                                + " that is not in the set.");
//                }
//            }
//        }
        
//        return packageScore;


//    private boolean isClassIncludedOverNeedle(Fingerprint classNeedle, Fingerprint classCandidate) {
//        
//        for(Fingerprint methodNeedle : classNeedle.getChildFingerprints()) {
//            for(Fingerprint methodCandidate : classCandidate.getChildFingerprints()) {
//
//                double sim = methodNeedle.getNonCommutativeSimilarityScoreToFingerprint(methodCandidate);
//                double simN = sim / methodNeedle.getLength();
//
//                if(simN > methodSimThresold) {
//                    if (isClassIncluded(classNeedle, classCandidate)) {
//                        return true;
//                    } else {
//                        checkedClasses.add(classCandidate.getName());
//                    }
//                }
//            }
//        }
//    }

    
//    @Override
//    public double computeInclusion(List<Fingerprint> superSet, List<Fingerprint> subSet) {
//        
//        logHeader();
//        
//        List<Fingerprint> classCandidates = new LinkedList<>(superSet);
//        
//        if(subSet.isEmpty()) {
//            return 0;
//        }
//        
//        for(Fingerprint classNeedle : subSet) {
//            for(Iterator<Fingerprint> it = classCandidates.iterator(); it.hasNext();) {
//                
//                Fingerprint classCandidate = it.next();
//                boolean foundClass = false;
//                
//                if(classCandidate.getChildFingerprints().size() < classNeedle.getChildFingerprints().size()) {
//                    continue;
//                }
//                
//                foundClass = isClassIncludedOverNeedle(classNeedle, classCandidate);
//                
//                
//                
//                
//                
//                
//                
//            }
//        }
//        
//        return 0;
//
//    }
//    