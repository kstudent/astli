package org.androidlibid.proto.match;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.androidlibid.proto.Fingerprint;
import org.androidlibid.proto.PackageHierarchy;
import org.androidlibid.proto.ao.FingerprintService;
import org.androidlibid.proto.ao.PackageHierarchyService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class HybridStrategy extends MatchingStrategy {

    private static final Logger LOGGER = LogManager.getLogger(HybridStrategy.class);
    
    private final FingerprintService fpService; 
    
    public HybridStrategy(FingerprintService fpService) {
        this.fpService = fpService;
    }
    
    @Override
    public void matchHierarchies(Map<String, PackageHierarchy> hierarchies) throws SQLException {
        for (PackageHierarchy hierarchy : hierarchies.values()) {
            matchHierarchy(hierarchy);
        }
    }

    private void matchHierarchy(PackageHierarchy hierarchy) throws SQLException {
        List<Fingerprint> needleMethods = distillMethodsWithHighEntropy(hierarchy, 10);
        LOGGER.info("** matching {}", hierarchy.getName());

        for(Fingerprint needle : needleMethods) {
            List<Fingerprint> candidateMethods = fpService.findMethodsBySignature(needle.getSignature());
            
            boolean foundCandidate = false;
            
            for (Fingerprint candidate : candidateMethods) {
                if (doFingerprintsRepresentSameMethods(candidate, needle, hierarchy)) {
                    foundCandidate = true;                 
                    break;
                }
            }
            
            LOGGER.info("*** {} method with sig {} and length {} has {} candidates", 
                    foundCandidate ? "DONE" : "",
                    needle.getSignature(), needle.getLength(), candidateMethods.size());
            
            for (Fingerprint candidate : candidateMethods) {
                boolean candidateMatches = doFingerprintsRepresentSameMethods(candidate, needle, hierarchy);
                LOGGER.info("| {} | {} | {} |", candidateMatches? "X" : "" , 
                    candidate.getSignature(), candidate.getLength());
            }
            
            if (foundCandidate) break;
            
        }
    }

    private List<Fingerprint> distillMethodsWithHighEntropy(PackageHierarchy hierarchy, int amount) {
        List<Fingerprint> methods = new ArrayList<>();
        
        for(String className : hierarchy.getClassNames()) {
            Map<String, Fingerprint> classMethods = hierarchy.getMethodsByClassName(className);
            methods.addAll(classMethods.values());
        }
        
        Collections.sort(methods, Fingerprint.sortBySignatureAndLengthDesc);
        
        return methods.subList(0, Math.min(amount, methods.size()));
    }

    private boolean doFingerprintsRepresentSameMethods(Fingerprint candidate, 
            Fingerprint needle, PackageHierarchy hierarchy) {
        
        if (!candidate.getName().equals(needle.getName())) {
            return false; 
        }
        
        String candidateClass = fpService.getClassNameByFingerprint(candidate);
        String needleClass = hierarchy.getClassNameByMethod(needle);
        
        return candidateClass.equals(needleClass);
        
    }
    
}
