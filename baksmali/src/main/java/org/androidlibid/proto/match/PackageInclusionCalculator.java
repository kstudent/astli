package org.androidlibid.proto.match;

import java.util.LinkedList;
import java.util.List;
import org.androidlibid.proto.Fingerprint;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class PackageInclusionCalculator {
    
    private final ClassInclusionCalculator calculator; 

    public PackageInclusionCalculator(ClassInclusionCalculator calculator) {
        this.calculator = calculator;
    }
    
    public double computePackageInclusion(List<Fingerprint> classesOfPackageA, List<Fingerprint> classesOfPackageB) {
        
        List<Fingerprint> smallerSet, biggerSet;
        
        if(classesOfPackageA.size() < classesOfPackageB.size()) {
            smallerSet = classesOfPackageA;
            biggerSet  = new LinkedList<>(classesOfPackageB);
        } else {
            smallerSet = classesOfPackageB;
            biggerSet  = new LinkedList<>(classesOfPackageA);
        }
        
        if(smallerSet.isEmpty()) {
            return 0;
        }
        
        double packageScore = 0;
        int amountMethods = 0;

        for (Fingerprint clazz : smallerSet) {
            amountMethods += clazz.getChildren().size();
            
            double maxScore = -1;
            Fingerprint maxScoreClazz = null;
            
            for (Fingerprint clazzCandidate : biggerSet) {
                
                double score = calculator.computeClassInclusion(clazz.getChildren(), clazzCandidate.getChildren());
                
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
            
            packageScore += maxScore;
            if(!biggerSet.remove(maxScoreClazz)) {
                throw new RuntimeException("are you feeding the bugs again??");
            }
        }
        
        return packageScore / amountMethods;
        
        
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
