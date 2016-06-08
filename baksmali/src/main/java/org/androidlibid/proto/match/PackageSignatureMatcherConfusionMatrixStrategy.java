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
    private final PackageSignatureMatcher sigMatcher;
    private final PackageScoreMatcher scoreMatcher;
    
    public PackageSignatureMatcherConfusionMatrixStrategy(FingerprintService fpService) {
        this.fpService = fpService;
        HungarianAlgorithm hg = new HungarianAlgorithm();
        this.sigMatcher = new PackageSignatureMatcher(hg);
        this.scoreMatcher = new PackageScoreMatcher(hg);
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
        printLabels("apk_labels_names", 
                apkHierarchies.stream().map(h -> h.getName()));
        printLabels("lib_labels_names", 
                libHierarchies.stream().map(h -> h.getName()));
  
        LOGGER.info("cm = np.array([");
        
        apkHierarchies.stream().forEachOrdered(apkh -> {
            
            double maxScore = calculateHybridScore(apkh, apkh);
//            double maxScore = calculatePrintScore(apkh, apkh); //TODO
            StringBuilder row = new StringBuilder("[");
            
            libHierarchies.stream().forEachOrdered(libh -> {
                double score = calculateHybridScore(apkh, libh);
//                double score = calculatePrintScore(apkh, libh); //TODO
                row.append(score / maxScore).append(",");
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

    private double calculatePrintScore(PackageHierarchy apkh, PackageHierarchy libh) {
        double[][] costMatrix = new double[apkh.getClassesSize()][libh.getClassesSize()];
        return scoreMatcher.getScore(apkh, libh, costMatrix);
    }
    
    private double calculateHybridScore(PackageHierarchy apkh, PackageHierarchy libh) {
        boolean matched = sigMatcher.checkSignatureInclusion(apkh, libh);
        double[][] costMatrix = sigMatcher.getCost();
        double score = 0.0d;
        if(matched) {
            score = scoreMatcher.getScore(apkh, libh, costMatrix);
        }
        return score;
    }
}
