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
public class PackageInclusionCalculator {
    
    private final ClassInclusionCalculator calculator; 
    
    private static final Logger LOGGER = LogManager.getLogger(PackageInclusionCalculator.class.getName());
    
    public PackageInclusionCalculator(ClassInclusionCalculator calculator) {
        this.calculator = calculator;
    }
    
    public double computePackageInclusion(List<Fingerprint> superSet, List<Fingerprint> subSet) {
        
//        String interestingClassName = ".NTRUEncryptionKeyGenerationParameters";
        
        LOGGER.info("| class | matched | score | max |"); 
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
            clazzName = clazzName.substring(clazzName.lastIndexOf("."), clazzName.length());
//
//            if(!interestingClassName.equals(clazzName))
//                continue;
            
//            LOGGER.info("*** myself: {}, which has {} methods.", clazzName, clazz.getChildren().size()); 
            
            double perfectScore = calculator.computeClassInclusion(clazz.getChildren(), clazz.getChildren()); 
//            LOGGER.info("perfect score: {}", perfectScore); 
            
            double maxScore = -1;
            Fingerprint maxScoreClazz = null;
            
            for (Fingerprint clazzCandidate : superSetCopy) {
                
                double score = calculator.computeClassInclusion(clazzCandidate.getChildren(), clazz.getChildren());
                
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
            
            String bestMatchName = maxScoreClazz.getName();
            bestMatchName = bestMatchName.substring(bestMatchName.lastIndexOf("."), bestMatchName.length());
            
//            LOGGER.info("*** {} -> {} ({})", clazzName, bestMatchName, maxScore); 
            LOGGER.info("| {} | {} | {} | {}|", clazzName, bestMatchName, maxScore, perfectScore); 

            
            packageScore += maxScore;
            if(!superSetCopy.remove(maxScoreClazz)) {
                throw new RuntimeException("are you feeding the bugs again??");
            }
        }
        
        return packageScore;
        
        
//        for (Iterator<Fingerprint> subSetIt = classesOfPackageB.iterator(); subSetIt.hasNext(); ) {
//            Fingerprint classNeedle = subSetIt.next();
//            
//            amountMethods += classNeedle.getChildren().size();
//            
//            double maxClassScore = 0; 
//
//            for (Iterator<Fingerprint> superSetIt = classesOfPackageA.iterator(); superSetIt.hasNext(); ) {
//                Fingerprint classCandidate = superSetIt.next();
//            
//                List<Fingerprint> methodSubSet   = new LinkedList<>(classNeedle.getChildren());
//                List<Fingerprint> methodSuperSet = new LinkedList<>(classCandidate.getChildren());
//                
//                double classScore = computeClassInclusion(methodSuperSet, methodSubSet);
//                double classScoreNormalizied = classScore / methodSubSet.size(); 
//                
//                maxClassScore = (classScore > maxClassScore) ? classScore : maxClassScore; 
//                
//                if(classScoreNormalizied > classMatchThreshold) {
//                    superSetIt.remove();
//                    break;
//                }
//            }
//            
//            packageScore += maxClassScore;
//            
//        }
        
//        return (packageScore / amountMethods); 
    }
}
