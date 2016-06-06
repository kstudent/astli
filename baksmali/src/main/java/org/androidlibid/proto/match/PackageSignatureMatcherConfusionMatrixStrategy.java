package org.androidlibid.proto.match;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
        LOGGER.info("* confusion matrix ");
        
        LOGGER.info("import numpy as np");
        
        List<PackageHierarchy> apkHierarchies = hierarchies.values().stream()
                .sorted((that, other) -> Integer.compare(that.getEntropy(), other.getEntropy()))
                .collect(Collectors.toList());
        
        List<PackageHierarchy> libHierarchies = fpService.getPackageHierarchies()
                .sorted((that, other) -> Integer.compare(that.getEntropy(), other.getEntropy()))
                .collect(Collectors.toList());
       
        killMissingHierarchies(apkHierarchies, libHierarchies);
        
        printLabels("apk_labels", 
                apkHierarchies.stream().map(h -> "" + h.getEntropy()));
        printLabels("lib_labels", 
                libHierarchies.stream().map(h -> "" + h.getEntropy()));
  
        LOGGER.info("cm = np.array([");
        
        apkHierarchies.stream().forEachOrdered(apkh -> {
            
            StringBuilder row = new StringBuilder("[");
            
            libHierarchies.stream().forEachOrdered(libh -> {
                boolean matched = matcher.checkSignatureInclusion(apkh, libh);
                row.append(matched ? "1" : "0").append(",");
            });
            
            row.append("], ");
            LOGGER.info(row.toString());
            
        });
        
        LOGGER.info("])");
    }
    
    private void printLabels(String varName, Stream<String> hierarchies) {
        if(LOGGER.isInfoEnabled()) {
            StringBuilder label = new StringBuilder(varName + " = [");
            hierarchies.forEach(name -> label.append("'").append(name).append("', "));
            label.append("]");
            LOGGER.info(label.toString());
        }
    }

    private void killMissingHierarchies(List<PackageHierarchy> apks, List<PackageHierarchy> libs) {
        
        int i = 0;
        try {
            while(true) {

                if(!apks.get(i).getName().equals(libs.get(i).getName())) {
                    if(apks.get(i).getEntropy() < libs.get(i).getEntropy()) {
                        apks.remove(i);
                    } else {
                        libs.remove(i);
                    }
                } else {
                    i++;
                }
            }
        } catch(IndexOutOfBoundsException ex) {
        }
    }
}
