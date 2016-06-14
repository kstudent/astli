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
public class ConfusionMatrixStrategy extends MatchingStrategy {

    private static final Logger LOGGER = LogManager.getLogger(ConfusionMatrixStrategy.class);
    
    private final FingerprintService fpService; 
    private final PackageSignatureMatcher sigMatcher;
    private final PackageScoreMatcher scoreMatcher;
    private final ResultEvaluator evaluator;
    
    public ConfusionMatrixStrategy(FingerprintService fpService) {
        this.fpService = fpService;
        HungarianAlgorithm hg = new HungarianAlgorithm();
        this.sigMatcher = new PackageSignatureMatcher(hg);
        this.scoreMatcher = new PackageScoreMatcher(hg);
        this.evaluator = new ResultEvaluator();
    }
    
    @Override
    public void matchHierarchies(Stream<PackageHierarchy> hierarchies) throws SQLException {
        LOGGER.info("* confusion matrix ");
        
        LOGGER.info("import numpy as np");
        
        List<PackageHierarchy> apkHierarchies = hierarchies
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
        
        apkHierarchies.stream().map(apkh -> {
            
            StringBuilder row = new StringBuilder("[");
            
            List<ResultItem> items = matchHierarchy(apkh, libHierarchies, row)
                    .sorted((that, other) -> Double.compare(other.getScore(), that.getScore()))
                    .collect(Collectors.toList());
            
            row.append("], ");
            LOGGER.info(row.toString());
            
            return new Result(items, apkh, true);
        }).map(result -> evaluator.evaluateResult(result))
          .forEach(eval -> incrementStats(eval));
        
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
    
    private double calculateHybridScore(PackageHierarchy apkh, PackageHierarchy libh) {
        PackageSignatureMatcher.Result result = sigMatcher.checkSignatureInclusion(apkh, libh);
        
        double score = 0.0d;
        if(result.packageAIsIncludedInB()) {
            double[][] costMatrix = result.getCostMatrix();
            score = scoreMatcher.getScore(apkh, libh, costMatrix);
        }
        return score;
    }

    private Stream<ResultItem> matchHierarchy(PackageHierarchy apkh, List<PackageHierarchy> libHierarchies, StringBuilder row) {
        double maxScore = calculateHybridScore(apkh, apkh);
        
        return libHierarchies.stream()
            .map(libh -> {
                double score = calculateHybridScore(apkh, libh);
                row.append(score / maxScore).append(",");
                return new ResultItem(score / maxScore, libh.getName());
            });
    }
    
    private void pt(String what, long t1) {
        LOGGER.info("{} took {}ms", what, (System.currentTimeMillis() - t1));
    }
}
