package org.androidlibid.proto.match;

import org.androidlibid.proto.ao.FingerprintService;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.androidlibid.proto.Fingerprint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static org.androidlibid.proto.match.FingerprintMatcher.Result;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class MatchWithInclusionStrategy implements MatchingStrategy {

    private final FingerprintService service;
    private final PackageInclusionCalculator calculator;
    private final ResultEvaluator evaluator; 
    private final Settings settings;
    
    private static final Logger LOGGER = LogManager.getLogger(MatchWithInclusionStrategy.class.getName());

    public MatchWithInclusionStrategy(FingerprintService service, 
            PackageInclusionCalculator calculator, ResultEvaluator evaluator) {
        this(service, calculator, evaluator, new Settings());
    }
    
    public MatchWithInclusionStrategy(FingerprintService service, 
            PackageInclusionCalculator calculator, ResultEvaluator evaluator, 
            Settings settings) {
        this.service = service;
        this.calculator = calculator;
        this.evaluator = evaluator;
        this.settings = settings;
    }

    @Override
    public Map<Status, Integer> matchPrints(Map<String, Fingerprint> packagePrints) throws SQLException {
        Map<Status, Integer> stats = new HashMap<>();
        for(Status key : Status.values()) {
            stats.put(key, 0);
        }
        
        int count = 0;
        
        for(Fingerprint packageNeedle : packagePrints.values()) {
            
            LOGGER.info("{}%", ((float)(count++) / packagePrints.size()) * 100); 
            
            if(packageNeedle.getName().startsWith("android")) continue;
            if(packageNeedle.getName().equals("")) continue;
            
            Result matches = findMatchForPackage(packageNeedle);

            MatchingStrategy.Status result = evaluator.evaluateResult(packageNeedle, matches);
            stats.put(result, stats.get(result) + 1);
        }
        
        return stats;
        
    }
    
    private @Nullable Result findMatchForPackage(Fingerprint packageNeedle) throws SQLException {
      
        Result result = new Result();
        result.setNeedle(packageNeedle);
        
        List<Fingerprint> packageMatches = new ArrayList<Fingerprint>();
        result.setMatchesByDistance(packageMatches);
        
        double perfectScore = computeInclusionScore(packageNeedle, packageNeedle, true);
        packageNeedle.setInclusionScore(perfectScore);
        
        LOGGER.info("* {} ({})", packageNeedle.getName(), perfectScore); 
        
        checkPackageAgainstSimilarMethods(result);
        
        Collections.sort(packageMatches, new SortDescByInclusionScoreComparator());

        updateMatchByName(result);
        
        return result;
    }
    
    /**
     * Fetches similar methods from service and looks for a match
     * 
     * @param result (will be updated by this method)
     * @throws SQLException 
     */
    private void checkPackageAgainstSimilarMethods(Result result) throws SQLException {
        
        Fingerprint packageNeedle = result.getNeedle();
        
        for(Fingerprint classNeedle : packageNeedle.getChildFingerprints()) {
            for(Fingerprint methodNeedle : classNeedle.getChildFingerprints()) {

                double length = methodNeedle.getLength();
                double size   = length * (1 - settings.getMethodMatchThreshold());

                if(length < settings.getMinimalMethodLengthForNeedleLookup()) {
                    break;
                }

                LOGGER.info("** {} ({})", methodNeedle.getName(), length); 

                List<Fingerprint> methodHaystack = service.findMethodsByLength(length, size);                

                LOGGER.info("{} needles to check", methodHaystack.size()); 

                if(tryNeedle(result, methodNeedle, methodHaystack)) {
                    return;
                }
            }
        }  
    }

    /**
     * Checks if the method needle leads to a package match
     * 
     * @param result (will be updated by this method)
     * @param methodNeedle
     * @param methodHaystack
     * @return true if this needle lead to a package match
     */
    private boolean tryNeedle(Result result, Fingerprint methodNeedle, List<Fingerprint> methodHaystack) {
        
        Fingerprint packageNeedle = result.getNeedle();
        Collection<Fingerprint> matchedPackages = result.getMatchesByDistance();
        
        double perfectScore = packageNeedle.getInclusionScore();
        
        for(Fingerprint methodCandidate : methodHaystack) {
                    
            if (!isItWorthToCheckCandidate(methodNeedle, methodCandidate)) {
                continue;
            } 

            Fingerprint packageCandidate = service.getPackageHierarchyByMethod(methodCandidate);
            String packageCandidateName = packageCandidate.getName();

            if (isNameInCollection(packageCandidateName, matchedPackages)) {
                continue;
            }
            
            double packageScore = computeInclusionScore(packageNeedle, packageCandidate, false);

            matchedPackages.add(packageCandidate);

            if(packageCandidateName.equals(packageNeedle.getName())) {
                result.setMatchByName(packageCandidate);
            }

            logResult(packageNeedle.getName(), packageCandidateName, packageScore, packageScore / perfectScore);

            if((packageScore / perfectScore) > settings.getPackageMatchThreshold()) {
                return true; 
            }
        }
        
        return false;
        
    }

    private void logResult(String needle, String match, double packageScore, double packageScoreNormalized) {
        LOGGER.info("{} -> {} (sim: {} {})", needle, match, packageScore, packageScoreNormalized );
    }
    
    /**
     * Tells whether or not a package check for given method is worth it
     * 
     * @param methodNeedle
     * @param methodCandidate
     * @return true if its worth to check
     */
    private boolean isItWorthToCheckCandidate(Fingerprint methodNeedle, 
            Fingerprint methodCandidate) {
        
        double methodSimilarityScore = methodCandidate.getSimilarityScoreToFingerprint(methodNeedle);
        
        double maxLength = Math.max(methodNeedle.getLength(), methodCandidate.getLength());

        return (methodSimilarityScore / maxLength > settings.getMethodMatchThreshold());
    }
    
    private boolean isNameInCollection(String name, Collection<Fingerprint> collection) {

        for(Fingerprint print : collection) {
            if(print.getName().equals(name)) {
                return true;
            }
        }
        
        return false;
    }

    private double computeInclusionScore(Fingerprint packageNeedle, 
            Fingerprint packageCandidate, boolean warnClassMismatches) 
    {
        List<Fingerprint> classSubSet   = new LinkedList<>(packageNeedle.getChildFingerprints());
        List<Fingerprint> classSuperSet = new LinkedList<>(packageCandidate.getChildFingerprints());

        return calculator.computePackageInclusion(classSuperSet, classSubSet, warnClassMismatches);
    }

    private void updateMatchByName(Result result) throws SQLException {
        
        if(result.getMatchByName() == null) {
            
            Fingerprint packageNeedle = result.getNeedle();
            String packageName = packageNeedle.getName(); 
            
            List<Fingerprint> packagesWithSameName = service.findPackagesByName(packageName);
            
            if(!packagesWithSameName.isEmpty()) {
                Fingerprint matchByName = service.getPackageHierarchy(packagesWithSameName.get(0));
                
                double score = computeInclusionScore(packageNeedle, matchByName, true);
                matchByName.setInclusionScore(score);
                
                result.setMatchByName(matchByName);
            } else {
                LOGGER.info("{} is not in database.", packageName);
            }
        }
        
    }
    
    private class SortDescByInclusionScoreComparator implements Comparator<Fingerprint> {
        @Override
        public int compare(Fingerprint that, Fingerprint other) {
            double scoreNeedleThat  = that.getInclusionScore();
            double scoreNeedleOther = other.getInclusionScore();
            if (scoreNeedleThat > scoreNeedleOther) return -1;
            if (scoreNeedleThat < scoreNeedleOther) return  1;
            return 0;
        }
    }
    
    public static class Settings {
        
        private double methodMatchThreshold;
        private double packageMatchThreshold; 
        private double minimalMethodLengthForNeedleLookup;        

        public Settings() {
            this(0.9999d,  0.8d, 12);
        } 
        
        public Settings(double methodMatchThreshold, double packageMatchThreshold, 
                double minimalMethodLengthForNeedleLookup) {
            this.methodMatchThreshold = methodMatchThreshold;
            this.packageMatchThreshold = packageMatchThreshold;
            this.minimalMethodLengthForNeedleLookup = minimalMethodLengthForNeedleLookup;
        }

        public double getMethodMatchThreshold() {
            return methodMatchThreshold;
        }

        public double getPackageMatchThreshold() {
            return packageMatchThreshold;
        }

        public double getMinimalMethodLengthForNeedleLookup() {
            return minimalMethodLengthForNeedleLookup;
        }
    }
    
}
