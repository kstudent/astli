package org.androidlibid.proto.match;

import java.sql.SQLException;
import java.util.Map;
import org.androidlibid.proto.Fingerprint;
import org.androidlibid.proto.ao.FingerprintService;
import org.androidlibid.proto.utils.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.la4j.vector.dense.BasicVector;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class DebugObfuscationInvarianceStrategy extends MatchingStrategy {

    private final FingerprintService service;
    
    private static final Logger LOGGER = LogManager.getLogger(DebugObfuscationInvarianceStrategy.class.getName());


    public DebugObfuscationInvarianceStrategy(FingerprintService service) {
        this.service = service;
    }
    
    @Override
    public void matchPrints(Map<String, Fingerprint> packagePrints) throws SQLException {
        
        for(Fingerprint packageNeedle : packagePrints.values()) {
            
//            LOGGER.info("* {} ", packageNeedle.getName());
            
            for(Fingerprint packageCandidate : service.findPackagesByName(packageNeedle.getName())) {
                for(Fingerprint classNeedle : packageNeedle.getChildFingerprints()) {

                    findClassInPackages(classNeedle, packageCandidate); 
                }
            }
        }
        
    }

    private void findClassInPackages(Fingerprint classNeedle, Fingerprint packageCandidate) {
        
        Fingerprint packageHierarchy = service.getPackageHierarchy(packageCandidate);
        
        for (Fingerprint classCandidate : packageHierarchy.getChildFingerprints()) {
            if(classCandidate.getName().matches(classNeedle.getName())) {
                findMethods(classCandidate, classNeedle);
            }
        }
        
    }

    private void findMethods(Fingerprint classCandidate, Fingerprint classNeedle) {
        for(Fingerprint methodNeedle : classNeedle.getChildFingerprints()) {
            for(Fingerprint methodCandidate : classCandidate.getChildFingerprints()) {
                if(methodNeedle.getName().equals(methodCandidate.getName())) {
                    printObfuscationInvariance(methodNeedle, methodCandidate);
                }
            }
        }
    }

    private void printObfuscationInvariance(Fingerprint needle, Fingerprint candidate) {
//        LOGGER.info("* diff");
//        LOGGER.info("needle: {}"    , needle);
//        LOGGER.info("candidate: {}" , candidate);
        
        Fingerprint diff = new Fingerprint(candidate);
        
        diff.subtractFeatures(needle);
        diff.abs();
        
        double errorAbs = diff.getFeatureVector().manhattanNorm();
        
        if(errorAbs > 0) {
            LOGGER.info("*** Diff (by errorAbs) : {}", needle.getName());
            LOGGER.info("{}", needle);
            LOGGER.info("{}", candidate);
        } else {
            LOGGER.info("*** No Diff for {}", needle.getName());
        }
        
//        LOGGER.info("{}, ", toPythonArray(diff));
//        LOGGER.info("{}, ", diff);
//        LOGGER.info("({}, {}),", toPythonArray(needle), toPythonArray(candidate));
        
//        LOGGER.info("diff: {}", diff);
//        BasicVector normalizedV = (BasicVector) diff.getFeatureVector().divide(errorAbs);   
//        LOGGER.info("diff normalized: {}", new Fingerprint(normalizedV.toArray()));
        
                
//        LOGGER.info("manhattan : {}", errorAbs);
//        LOGGER.info("manhattan norm : {}", diff.getFeatureVector().manhattanNorm() / candidate.getFeatureVector().manhattanNorm());
//        LOGGER.info("euclid : {}",         diff.getFeatureVector().euclideanNorm());
//        LOGGER.info("euclid norm: {}",     diff.getFeatureVector().euclideanNorm() / candidate.getFeatureVector().euclideanNorm());
    }

    private String toPythonArray(Fingerprint diff) {
        StringBuilder builder = new StringBuilder("[");
        BasicVector diffV = (BasicVector) diff.getFeatureVector();   
        builder.append(StringUtils.implode(diffV.toArray(), ","));
        builder.append("] ");
        return builder.toString();
    }
    
}
