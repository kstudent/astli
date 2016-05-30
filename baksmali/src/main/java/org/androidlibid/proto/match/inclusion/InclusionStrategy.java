package org.androidlibid.proto.match.inclusion;

import org.androidlibid.proto.ao.FingerprintService;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.androidlibid.proto.Fingerprint;
import org.androidlibid.proto.match.Evaluation;
import org.androidlibid.proto.match.MatchingStrategy;
import org.androidlibid.proto.match.ResultEvaluator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static org.androidlibid.proto.match.FingerprintMatcher.Result;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class InclusionStrategy extends MatchingStrategy {

    private final FingerprintService service;
    private final InclusionCalculator calculator;
    private final ResultEvaluator evaluator; 
    private final InclusionStrategySettings settings;
    
    private final List<Fingerprint> prettySureMatches = new ArrayList<>();
    
    private final NumberFormat frmt = new DecimalFormat("#0.00");
    private static final Logger LOGGER = LogManager.getLogger(InclusionStrategy.class.getName());

    public InclusionStrategy(FingerprintService service, 
            InclusionCalculator calculator, ResultEvaluator evaluator) {
        this(service, calculator, evaluator, new InclusionStrategySettings());
    }
    
    public InclusionStrategy(FingerprintService service, 
            InclusionCalculator calculator, ResultEvaluator evaluator, 
            InclusionStrategySettings settings) {
        super();
        this.service = service;
        this.evaluator = evaluator;
        this.calculator = calculator;
        this.settings = settings;
    }

    

    @Override
    public void matchPrints(Map<String, Fingerprint> packagePrints) throws SQLException {
        
        LOGGER.info("* Match Prints");

        evaluator.printResultRowHeader();
        
        int count = 0;
        
        List<Fingerprint> packageNeedles = new ArrayList<>(packagePrints.values());
        Collections.sort(packageNeedles, Fingerprint.sortByLengthDESC);
        
        for(Fingerprint packageNeedle : packageNeedles) {
            
            LOGGER.info("{}%", ((float)(count++) / packagePrints.size()) * 100); 
            
            if(packageNeedle.getName().startsWith("android")) continue;
            if(packageNeedle.getName().equals("")) continue;
            
            Result result = findMatchForPackage(packageNeedle);

            Evaluation evaluation = evaluator.evaluateResult(result);
            
            incrementStats(evaluation);
            
        }
    }
    
    private @Nullable Result findMatchForPackage(Fingerprint packageNeedle) throws SQLException {
      
//        Result result = new Result();
//        result.setNeedle(packageNeedle);
//        
//        List<MethodFingerprint> packageMatches = new ArrayList<MethodFingerprint>();
//        result.setMatchesByDistance(packageMatches);
//        
//        LOGGER.info("* {} ", packageNeedle.getName()); 
//        
//        LOGGER.info("** package self check "); 
//        double perfectScore = computeInclusionScore(packageNeedle, packageNeedle);
//        packageNeedle.setComputedSimilarityScore(perfectScore);
//
//        LOGGER.info("** package self check score: {}", frmt.format(perfectScore)); 
//        
//        checkPackageAgainstSimilarMethods(result);
//        
//        removeRejectedMatches(packageMatches, perfectScore);
//        
//        Collections.sort(packageMatches, MethodFingerprint.sortBySimScoreDESC);
//
//        updateMatchByName(result);
//        
//        return result;
        return null;
    }
    
    /**
     * Fetches similar methods from service and looks for a match
     * 
     * @param result (will be updated by this method)
     * @throws SQLException 
     */
    private void checkPackageAgainstSimilarMethods(Result result) throws SQLException {
        
        Fingerprint packageNeedle = result.getNeedle();
        
        List<Fingerprint> methodNeedles = distillMethodNeedles(packageNeedle);
        
        for(Fingerprint methodNeedle : methodNeedles) {

            double length = methodNeedle.getLength();
            double size   = length * (1 - settings.getMethodAcceptThreshold());
                
            LOGGER.info("** needle: {} ({})", methodNeedle.getName(), frmt.format(length)); 

            List<Fingerprint> methodHaystack = service.findMethodsByLength(length, size);                

            LOGGER.info("{} similar needles to check", methodHaystack.size()); 

            if(tryFindingNeedleInHaystack(result, methodNeedle, methodHaystack)) {
                return;
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
    private boolean tryFindingNeedleInHaystack(Result result, Fingerprint methodNeedle, List<Fingerprint> methodHaystack) {
        
        Fingerprint packageNeedle = result.getNeedle();
//        Collection<MethodFingerprint> comparedMatches = result.getMatchesByDistance();
//        
//        double perfectScore = packageNeedle.getComputedSimilarityScore();
//        
//        for(MethodFingerprint methodCandidate : methodHaystack) {
//            
//            if (!isItWorthToCheckCandidate(methodNeedle, methodCandidate)) {
//                continue;
//            } 
//
//            MethodFingerprint packageCandidate = service.getPackageByMethod(methodCandidate);
//            String packageCandidateName = packageCandidate.getName();
//
//            if (isNameInCollection(packageCandidateName, comparedMatches) 
//                    || isNameInCollection(packageCandidateName, prettySureMatches)) {
//                continue;
//            }
//            
//            LOGGER.info("*** check against db version of {}", packageCandidateName);
//            
//            MethodFingerprint packageHierarchy = service.getPackageHierarchy(packageCandidate);
//            
//            double packageScore = computeInclusionScore(packageNeedle, packageHierarchy);
//            
//            LOGGER.info("*** check against db version of {} done", packageCandidateName);
//
//            packageCandidate.setComputedSimilarityScore(packageScore);
//            comparedMatches.add(packageCandidate);
//
//            if(packageCandidateName.equals(packageNeedle.getName())) {
//                result.setMatchByName(packageCandidate);
//            }
//
//            double normalizedScore = packageScore / perfectScore;
//            
//            logResult(packageNeedle.getName(), packageCandidateName, packageScore, normalizedScore);
//
//            if(normalizedScore > settings.getPackageAcceptThreshold(packageScore)) {
//                prettySureMatches.add(packageCandidate);
//                return true; 
//            } 
//        }
        
        return false;
        
    }

    private void logResult(String needle, String match, double packageScore, double packageScoreNormalized) {
        LOGGER.info("{} -> {} (sim: {} {})", needle, match, frmt.format(packageScore), frmt.format(packageScoreNormalized));
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

        return (methodSimilarityScore / maxLength > settings.getMethodAcceptThreshold());
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
            Fingerprint packageCandidate) 
    {
//        List<MethodFingerprint> classSubSet   = new LinkedList<>(packageNeedle.getChildFingerprints());
//        List<MethodFingerprint> classSuperSet = new LinkedList<>(packageCandidate.getChildFingerprints());
//
//        return calculator.computeInclusion(classSuperSet, classSubSet);
        return 0;
    }

    private void updateMatchByName(Result result) throws SQLException {
        
//        if(result.getMatchByName() == null) {
//            
//            MethodFingerprint packageNeedle = result.getNeedle();
//            String packageName = packageNeedle.getName(); 
//            
//            List<MethodFingerprint> packagesWithSameName = service.findPackagesByName(packageName);
//            
//            if(!packagesWithSameName.isEmpty()) {
//                MethodFingerprint matchByName = service.getPackageHierarchy(packagesWithSameName.get(0));
//                
//                double score = computeInclusionScore(packageNeedle, matchByName);
//                matchByName.setComputedSimilarityScore(score);
//                
//                result.setMatchByName(matchByName);
//            } else {
//                LOGGER.info("{} is not in database.", packageName);
//            }
//        }
    }

    private List<Fingerprint> distillMethodNeedles(Fingerprint packageNeedle) {
        
        List<Fingerprint> methodNeedles = new ArrayList<>();
        
//        for(MethodFingerprint classNeedle : packageNeedle.getChildFingerprints()) {
//            for(MethodFingerprint methodNeedle : classNeedle.getChildFingerprints()) {
//                if(methodNeedle.getLength() > settings.getMinimalMethodLengthForNeedleLookup()) {
//                   methodNeedles.add(methodNeedle);
//                }
//            }
//        }
//        
//        Collections.sort(methodNeedles, MethodFingerprint.sortByLengthDESC);
       
        return methodNeedles;
        
    }

    private void removeRejectedMatches(List<Fingerprint> matches, double perfectScore) {
//        for (Iterator<MethodFingerprint> iterator = matches.iterator(); iterator.hasNext();) {
//            MethodFingerprint match = iterator.next();
//            
//            double normalizedScore = match.getComputedSimilarityScore() / perfectScore;
//            
//            if(normalizedScore < settings.getPackageRejectThreshold()) {
//                iterator.remove();
//            }
//        }
    }
    
    
}
