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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class HybridStrategy extends MatchingStrategy {

    private static final Logger LOGGER = LogManager.getLogger(HybridStrategy.class);
    private static final NumberFormat FRMT = new DecimalFormat("#0.00");

    private final FingerprintService fpService;
    private final PackageSignatureMatcher sigMatcher;
    private final PackageScoreMatcher scoreMatcher;
    private final ResultEvaluator evaluator;
    
    private final Map<String, List<PackageHierarchy>> hierarchyCache;

    private final int minimalNeedleEntropy;

    public HybridStrategy(FingerprintService fpService, int minimalNeedleLength, ResultEvaluator evaluator) {
        this.fpService = fpService;
        this.minimalNeedleEntropy = minimalNeedleLength;
        this.sigMatcher = new PackageSignatureMatcher(new HungarianAlgorithm());
        this.scoreMatcher = new PackageScoreMatcher(new HungarianAlgorithm());
        this.evaluator = evaluator;
//        this.hierarchyCache = new MapMaker().weakValues().makeMap(); //memory friendly but slower
        this.hierarchyCache = new HashMap<>();
    }

    @Override
    public void matchHierarchies(Map<String, PackageHierarchy> hierarchies) throws SQLException {
        LOGGER.info("* matching hierarchies"   );
        
        hierarchies.values().stream()
                .map(apkH -> {
                    List<ResultItem> matches = matchHierarchy(apkH);
                    return new Result(matches, apkH, fpService.isPackageInDB(apkH.getName()));
                })
                .map(result -> evaluator.evaluateResult(result))
                .forEach(eval -> incrementStats(eval));

    }

    private List<ResultItem> matchHierarchy(PackageHierarchy apkH) {

        final double maxScore = calculateHybridScore(apkH, apkH);

        return distillMethodsWithHighEntropy(apkH)
                .limit(10)
                .flatMap(needle -> fpService.findSameMethods(needle))
                .map(candidate -> fpService.getPackageNameByFingerprint(candidate))
                .distinct()
                .flatMap(name -> getPackageHierarchyCandidates(name))
                .map(libH -> {
                    double score = calculateHybridScore(apkH, libH) / maxScore;
                    return new ResultItem(score, libH.getName());
                })
                .sorted((that, other) -> Double.compare(other.getScore(), that.getScore()))
                .collect(Collectors.toList());
    }

    public Stream<Fingerprint> distillMethodsWithHighEntropy(PackageHierarchy hierarchy) {
        return hierarchy.getClassNames().parallelStream()
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

    private double calculateHybridScore(PackageHierarchy apkh, PackageHierarchy libh) {
        PackageSignatureMatcher.Result result = sigMatcher.checkSignatureInclusion(apkh, libh);

        double score = 0.0d;
        if(result.packageAIsIncludedInB()) {
            score = scoreMatcher.getScore(apkh, libh, result.getCostMatrix());
        }
        return score;
    }

    private synchronized Stream<PackageHierarchy> getPackageHierarchyCandidates(String name) {
        if (!hierarchyCache.containsKey(name)) {
            List hierarchies = fpService.getPackageHierarchiesByName(name).collect(Collectors.toList());
            hierarchyCache.put(name, hierarchies);
            incDbLookup();
        }
        
        return hierarchyCache.get(name).stream();
    }
}
