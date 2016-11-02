package org.androidlibid.proto.match;

import com.google.common.collect.MapMaker;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.androidlibid.proto.Fingerprint;
import org.androidlibid.proto.PackageHierarchy;
import org.androidlibid.proto.ao.FingerprintService;
import org.androidlibid.proto.ao.Package;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class HybridAlternativeStrategy extends MatchingStrategy {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final NumberFormat FRMT = new DecimalFormat("#0.00");

    private final FingerprintService fpService;
    private final PackageSignatureMatcher sigMatcher;
    private final PackageScoreMatcher scoreMatcher;
    private final ResultEvaluator evaluator;
    
    private final int minimalNeedleEntropy;

    public HybridAlternativeStrategy(FingerprintService fpService, int minimalNeedleLength, ResultEvaluator evaluator) {
        this.fpService = fpService;
        this.minimalNeedleEntropy = minimalNeedleLength;
        this.sigMatcher = new PackageSignatureMatcher(new HungarianAlgorithm());
        this.scoreMatcher = new PackageScoreMatcher(new HungarianAlgorithm());
        this.evaluator = evaluator;
    }

    @Override
    public void matchHierarchies(Stream<PackageHierarchy> hierarchies) throws SQLException {
        
        List<Package> libHierachies = hierarchies
            .flatMap(apkH -> distillMethodsWithHighEntropy(apkH))
            .flatMap(needle -> fpService.findSameMethods(needle))
            .map(candidate -> candidate.getMethod().getClazz().getPackage())
            .distinct()
            .collect(Collectors.toList());
        
        
        
//      List<PackageHierarchy>
//            .map(pckg -> fpService.createHierarchyFromPackage(pckg))
//            .collect(Collectors.toList());
        
//        hierarchies.values().parallelStream()
//            .map(apkH -> matchHierarchy(apkH, libHierachies))
//            .map(result -> evaluator.evaluateResult(result))
//            .forEach(evaluation -> incrementStats(evaluation));
    }
    
    private Result matchHierarchy(PackageHierarchy apkH, List<PackageHierarchy> libHierachies) {
        
        double maxScore = calculateHybridScore(apkH, apkH);
        
        List<ResultItem> items = libHierachies.parallelStream()
            .map(libH -> {
                    double score = calculateHybridScore(apkH, libH);
                    return new ResultItem(score / maxScore, libH.getName(), libH.getEntropy());
                })
            .sorted((that, other) -> Double.compare(other.getScore(), that.getScore()))
            .collect(Collectors.toList());
        
        return new Result(items, apkH, true);
    }
    
    public Stream<Fingerprint> distillMethodsWithHighEntropy(PackageHierarchy hierarchy) {
        return hierarchy.getClassNames().parallelStream()
                .map(name -> hierarchy.getMethodsByClassName(name))
                .flatMap(methods -> methods.values().stream())
                .filter(method -> method.getEntropy() > minimalNeedleEntropy)
                .sorted((that, othr) -> (-1) * Integer.compare(that.getEntropy(), othr.getEntropy()));
    }
    
    private double calculateHybridScore(PackageHierarchy apkh, PackageHierarchy libh) {
        PackageSignatureMatcher.Result result = sigMatcher.checkSignatureInclusion(apkh, libh);

        double score = 0.0d;
        if (result.packageAIsIncludedInB()) {
            score = scoreMatcher.getScore(apkh, libh, result.getCostMatrix());
        }
        return score;
    }
    
    private void pt(String what, long t1) {
        LOGGER.info("| {} | {} |", what, (System.currentTimeMillis() - t1));
    }
}
