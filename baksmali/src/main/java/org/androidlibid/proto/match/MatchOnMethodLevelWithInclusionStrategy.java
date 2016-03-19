package org.androidlibid.proto.match;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.androidlibid.proto.Fingerprint;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class MatchOnMethodLevelWithInclusionStrategy implements MatchingStrategy {

    private final FingerprintService service;
    private final FingerprintMatcher matcher;
    private final NumberFormat frmt = new DecimalFormat("#0.00");
    private final double methodThreshold = 0.001d;
    private final double classMatchThreshold = 0.8d;
    private final double packageMatchThreshold = 0.8d;

    public MatchOnMethodLevelWithInclusionStrategy(FingerprintService service, FingerprintMatcher matcher) {
        this.service = service;
        this.matcher = matcher;
    }

    @Override
    public Map<Status, Integer> matchPrints(Map<String, Fingerprint> packagePrints) throws SQLException {
        Map<Status, Integer> stats = new HashMap<>();
        for(Status key : Status.values()) {
            stats.put(key, 0);
        }
        
        int count = 0;
        
        for(Fingerprint packageNeedle : packagePrints.values()) {
            
            System.out.println(((float)(count++) / packagePrints.size()) * 100 + "%"); 
            
            if(packageNeedle.getName().startsWith("android")) continue;
            if(packageNeedle.getName().equals("")) continue;
         
            int level = StringUtils.countMatches(packageNeedle.getName(), ".");
            
            List<Fingerprint> methodHayStack = service.findMethodsByPackageDepth(level);
            
            Fingerprint packageCandidate = findPackageInMethodHaystack(packageNeedle, methodHayStack);
            
            if(packageCandidate == null) {
                System.out.println("no match for " + packageNeedle.getName() );
                stats.put(Status.NO_MATCH_BY_DISTANCE, stats.get(Status.NO_MATCH_BY_DISTANCE) + 1);
            } else {
                System.out.println("match for " + packageNeedle.getName() + ": " + packageCandidate.getName());
                stats.put(Status.OK, stats.get(Status.OK) + 1);
            }
        }
        
        return stats;
        
    }

    private @Nullable Fingerprint findPackageInMethodHaystack(Fingerprint packageNeedle, List<Fingerprint> methodHaystack) {
        
        for(Fingerprint classNeedle : packageNeedle.getChildren()) {
            for(Fingerprint methodNeedle : classNeedle.getChildren()) {
                for(Fingerprint methodCandidate : methodHaystack) {
                    if(methodCandidate.euclideanDiff(methodNeedle) < methodThreshold) {
                        
                        System.out.println("...found two similar methods:");
                        System.out.println("    " + methodNeedle.getName() + " -> " + methodCandidate.getName() + "(" + methodCandidate.euclideanDiff(methodNeedle) + ")"); 
                        
                        Fingerprint classCandidate = methodCandidate.getParent();

                        List<Fingerprint> methodSuperSet = new LinkedList<>(classCandidate.getChildren());
                        List<Fingerprint> methodSubSet   = new LinkedList<>(classNeedle.getChildren());

                        methodSuperSet.remove(methodCandidate);
                        methodSubSet.remove(methodNeedle);

                        double classScore = checkClassInclusion(methodSuperSet, methodSubSet);

                        System.out.println("    class inclusion between " + classNeedle.getName() + " and " + classCandidate.getName() + ": " + classScore);
                        
                        if(classScore > classMatchThreshold) {
                            Fingerprint packageCandiate = classCandidate.getParent();

                            if(packageCandiate == null) {
                                throw new RuntimeException("Check your hierarchy!");
                            }

                            List<Fingerprint> classSuperSet = new LinkedList<>(packageCandiate.getChildren());
                            List<Fingerprint> classSubSet   = new LinkedList<>(packageNeedle.getChildren());

                            classSuperSet.remove(classCandidate);
                            classSubSet.remove(classNeedle);

                            double packageScore = checkPackageInclusion(classSuperSet, classSubSet);
                            System.out.println("    package inclusion for " + packageNeedle.getName() + " and " + packageCandiate.getName() + ": " + packageScore);

                            if(packageScore > packageMatchThreshold) {
                                return packageCandiate;
                            }
                        }
                    }
                }
            }
        }    
        
        return null;
    }

    private double checkClassInclusion(List<Fingerprint> superSet, List<Fingerprint> subSet) {
        
        if(subSet.isEmpty()) {
            return 1;
        }
        
        int matches = 1;
        
        for (Fingerprint element : subSet) {
            FingerprintMatcher.Result result = matcher.matchFingerprints(superSet, element);
            
            if(result.getMatchesByDistance().size() > 0 && element.euclideanDiff(result.getMatchesByDistance().get(0)) < methodThreshold) {
                matches++;
            }
        }
        
        return ((double) matches / (subSet.size() + 1)); 
    }

    private double checkPackageInclusion(List<Fingerprint> superSet, List<Fingerprint> subSet) {
        if(subSet.isEmpty()) {
            return 1;
        }
        
        double packageScore = 1;

        for (Iterator<Fingerprint> subSetIt = subSet.iterator(); subSetIt.hasNext(); ) {
            Fingerprint classNeedle = subSetIt.next();
            
            double maxClassScore = 0; 

            for (Iterator<Fingerprint> superSetIt = superSet.iterator(); superSetIt.hasNext(); ) {
                Fingerprint classCandidate = superSetIt.next();
            
                List<Fingerprint> methodSubSet   = new LinkedList<>(classNeedle.getChildren());
                List<Fingerprint> methodSuperSet = new LinkedList<>(classCandidate.getChildren());
                double classScore = checkClassInclusion(methodSuperSet, methodSubSet);
                maxClassScore = (classScore > maxClassScore) ? classScore : maxClassScore; 
                
                if(classScore > classMatchThreshold) {
                    superSetIt.remove();
                    break;
                }
            }
            
            packageScore += maxClassScore;
            
        }
        
        return (packageScore / (subSet.size() + 1)); 
    }

}
