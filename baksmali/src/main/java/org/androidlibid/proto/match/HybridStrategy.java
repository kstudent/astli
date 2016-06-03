package org.androidlibid.proto.match;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.androidlibid.proto.Fingerprint;
import org.androidlibid.proto.PackageHierarchy;
import org.androidlibid.proto.ao.FingerprintService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class HybridStrategy extends MatchingStrategy {

    private static final Logger LOGGER = LogManager.getLogger(HybridStrategy.class);
    
    private final FingerprintService fpService; 
    private final PackageSignatureMatcher matcher; 
    
    private final float methodNeedleSimThreshold = 0.99f;
    private final int minimalNeedleEntropy;
    
    public HybridStrategy(FingerprintService fpService, int minimalNeedleLength) {
        this.fpService = fpService;
        this.minimalNeedleEntropy = minimalNeedleLength;
        this.matcher = new PackageSignatureMatcher();
    }
    
    @Override
    public void matchHierarchies(Map<String, PackageHierarchy> hierarchies) throws SQLException {
        LOGGER.info("* matching hierarchies!"   );
        for (PackageHierarchy hierarchy : hierarchies.values()) {
            matchHierarchy(hierarchy);
        }
    }
    
    private void matchHierarchy(PackageHierarchy hierarchy) throws SQLException {

        boolean packageMatched = false;
        
        List<Fingerprint> needles =  distillMethodsWithHighEntropy(hierarchy)
                .limit(10)
                .collect(Collectors.toList());
        
        LOGGER.info("** {} has {} needles...", hierarchy.getName(), needles.size());
        
        int i = 0; 
        
        for(Fingerprint needle : needles) {
            
            List<Fingerprint> candidates = fpService.findSameMethods(needle);
            
            LOGGER.info("needle {} (e: {}) has {} candidates", 
                    needle.getSignature(), needle.getEntropy(), candidates.size());
           
            for(Fingerprint candidate : candidates) {
                
                i++;
                
                boolean signatureInclusiveCandidate = checkSignatureInclusion(candidate, needle, hierarchy);

                LOGGER.info("candiate {} with size {}... {}", 
                        candidate.getSignature(),
                        candidate.getLength(),
                        signatureInclusiveCandidate ? "CHECK" : ""
                );
                
                if(signatureInclusiveCandidate) {
                    packageMatched = true;
                    break;
                }
            }
            
            if(packageMatched) break;
        }
        
        LOGGER.info("** {} {}", (packageMatched) ? "DONE" : "NEXT", i);
            
    }

    public Stream<Fingerprint> distillMethodsWithHighEntropy(PackageHierarchy hierarchy) {
        return hierarchy.getClassNames().stream()
            .map(name -> hierarchy.getMethodsByClassName(name))
            .flatMap(methods -> methods.values().stream())
            .filter(method -> method.getLength() > minimalNeedleEntropy)
            .sorted((that, othr) -> (-1) * Integer.compare(that.getEntropy(), othr.getEntropy()));
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

    private boolean checkSignatureInclusion(Fingerprint candidate, Fingerprint needle, PackageHierarchy hierarchy) {
        PackageHierarchy candidateHierarchy = fpService.getPackageHierarchyByFingerprint(candidate);
        return matcher.checkSignatureInclusion(hierarchy, candidateHierarchy);
    }
    
}
