package org.androidlibid.proto.match;

import java.sql.SQLException;
import java.util.Map;
import org.androidlibid.proto.PackageHierarchy;
import org.androidlibid.proto.ao.FingerprintService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class PackageSignatureMatcherConfusionMatrixStrategy extends MatchingStrategy {

    private static final Logger LOGGER = LogManager.getLogger(PackageSignatureMatcherConfusionMatrixStrategy.class);
    
    private final FingerprintService fpService; 
    private final PackageSignatureMatcher matcher;
    
    public PackageSignatureMatcherConfusionMatrixStrategy(FingerprintService fpService) {
        this.fpService = fpService;
        this.matcher = new PackageSignatureMatcher();
    }
    
    @Override
    public void matchHierarchies(Map<String, PackageHierarchy> hierarchies) throws SQLException {
        LOGGER.info("* confusion matrix..."   );
        for (PackageHierarchy hierarchy : hierarchies.values()) {
            matchHierarchy(hierarchy);
        }
    }
    
    private void matchHierarchy(PackageHierarchy hierarchy) throws SQLException {
        LOGGER.info("** doing {} ", hierarchy.getName());
        
        fpService.getPackageHierarchies().forEach(candidate -> {
            boolean matched = matcher.checkSignatureInclusion(hierarchy, candidate);
            if(matched != (hierarchy.getName().equals(candidate.getName()))) {
                LOGGER.info("*** NEXT mismatch with {} -> {}", matched ? "+" : "-", candidate.getName());
            } else {
                LOGGER.info("*** {} ok", candidate.getName());
            
            }
        });
    }
}
