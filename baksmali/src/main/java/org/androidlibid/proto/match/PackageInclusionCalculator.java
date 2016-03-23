package org.androidlibid.proto.match;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.androidlibid.proto.Fingerprint;
import org.androidlibid.proto.logger.MyLogger;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class PackageInclusionCalculator {
    
    private final ClassInclusionCalculator calculator; 
    
    private static final Logger LOG = MyLogger.getLogger(PackageInclusionCalculator.class.getName());
    
    public PackageInclusionCalculator(ClassInclusionCalculator calculator) {
        this.calculator = calculator;
    }
    
    public double computePackageInclusion(List<Fingerprint> superSet, List<Fingerprint> subSet) {
        
        List<Fingerprint> superSetCopy = new LinkedList<>(superSet);
        
        if(subSet.isEmpty()) {
            return 0;
        }
        
        double packageScore = 0;

        for (Fingerprint clazz : subSet) {
            
            if (superSetCopy.isEmpty()) {
                break;
            }
            
            LOG.fine("*** myself: " + clazz.getName() + ", which has " + clazz.getChildren().size() + " methods."); 
            double perfectScore = calculator.computeClassInclusion(clazz.getChildren(), clazz.getChildren()); 
            LOG.fine("perfect score: " + perfectScore); 
            
            double maxScore = -1;
            Fingerprint maxScoreClazz = null;
            
            for (Fingerprint clazzCandidate : superSetCopy) {
                LOG.finer("**** " + clazzCandidate.getName()); 
                
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
            
            String clazzName = clazz.getName();
            clazzName = clazzName.substring(clazzName.lastIndexOf("."), clazzName.length());
            String bestMatchName = maxScoreClazz.getName();
            bestMatchName = bestMatchName.substring(bestMatchName.lastIndexOf("."), bestMatchName.length());
            
            LOG.fine("*** result: " + bestMatchName); 
            LOG.fine("| " + clazzName + " | " + bestMatchName + " | " + maxScore + " | "); 
            
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
