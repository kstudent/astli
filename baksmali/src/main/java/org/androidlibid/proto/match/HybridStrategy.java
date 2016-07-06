package org.androidlibid.proto.match;

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
import org.androidlibid.proto.ao.Package;

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
    
    private final double minScoreThreshold   = .5;
    private final double minEntropyThreshold = 10;
    
    private final Map<Package, PackageHierarchy> hierarchyCache;

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
    public void matchHierarchies(Stream<PackageHierarchy> hierarchies) throws SQLException {
        LOGGER.info("** matching hierarchies"   );
        
        hierarchies.map(apkH -> {
                LOGGER.debug("*** {} (E:{})", apkH.getName(), apkH.getEntropy());
                    List<ResultItem> matches = matchHierarchy(apkH);
                    return new Result(matches, apkH, fpService.isPackageInDB(apkH.getName()));
                })
                .peek(result -> debugResult(result))
                .map(result -> evaluator.evaluateResult(result))
                .forEach(eval -> incrementStats(eval));

    }

    private List<ResultItem> matchHierarchy(PackageHierarchy apkH) {

        final double maxScore = calculateHybridScore(apkH, apkH);

        List<Package> candidates = distillMethodsWithHighEntropy(apkH)
                .limit(10)
                .peek(needle -> LOGGER.debug("needle: {} ({})", needle.getName(), needle.getEntropy()))
                .flatMap(needle -> fpService.findPackagesWithSameMethods(needle))
                .distinct()
                .collect(Collectors.toList());
        
        LOGGER.debug("{} has {} package candidates(s)", apkH.getName(), candidates.size());
        
        return candidates.stream()
                .map(pckg -> getPackageHierarchyCandidate(pckg))
                .map(libH -> {
                    double score = calculateHybridScore(apkH, libH) / maxScore;
                    return new ResultItem(score, libH.getName(), libH.getEntropy());
                })
                .peek(item -> LOGGER.debug("{} -> {}: {}", apkH.getName(), item.getPackage(), FRMT.format(item.getScore())))
                .filter(item -> meetsMinimalMatchingRequirements(item))
                .sorted((that, other) -> Double.compare(other.getScore(), that.getScore()))
                .collect(Collectors.toList());
    }

    Stream<Fingerprint> distillMethodsWithHighEntropy(PackageHierarchy hierarchy) {
        return hierarchy.getClassNames().parallelStream()
                .map(name -> hierarchy.getMethodsByClassName(name))
                .flatMap(methods -> methods.values().stream())
                .filter(method -> method.getEntropy() > minimalNeedleEntropy)
                .sorted((that, othr) -> (-1) * Integer.compare(that.getEntropy(), othr.getEntropy()));
    }

    private double calculateHybridScore(PackageHierarchy apkh, PackageHierarchy libh) {
        PackageSignatureMatcher.Result result = sigMatcher.checkSignatureInclusion(apkh, libh);

        double score = 0.0d;
        if(result.packageAIsIncludedInB()) {
            score = scoreMatcher.getScore(apkh, libh, result.getCostMatrix());
        }
        return score;
    }

    private synchronized PackageHierarchy getPackageHierarchyCandidate(Package pckg) {
        if (!hierarchyCache.containsKey(pckg)) {
            PackageHierarchy hierarchy = fpService.createHierarchyFromPackage(pckg);
            hierarchyCache.put(pckg, hierarchy);
            incDbLookup();
        }
        
        return hierarchyCache.get(pckg);
    }
    
    boolean doFingerprintsRepresentSameMethods(Fingerprint candidate,
            Fingerprint needle, PackageHierarchy hierarchy) {

        if (!candidate.getName().equals(needle.getName())) {
            return false;
        }

        String candidateClass = fpService.getClassNameByFingerprint(candidate);
        String needleClass = hierarchy.getClassNameByMethod(needle);

        return candidateClass.equals(needleClass);

    }
    
    private boolean meetsMinimalMatchingRequirements(ResultItem item) {
        return item.getScore() > minScoreThreshold && item.getEntropy() > minEntropyThreshold;
    }

    private void debugResult(Result result) {
        
        if(LOGGER.isDebugEnabled()) {
            
            PackageHierarchy apkH = result.getApkH();
            boolean inDB = result.isPackageInDB();
            
            LOGGER.debug("*** self apk check: {}", apkH.getName());
            
            double maxScore = calculateHybridScore(apkH, apkH);
            LOGGER.debug("{} -> {} : {}", apkH.getName(), apkH.getName(), FRMT.format(maxScore));
            
            if(inDB) {
                LOGGER.debug("*** self db check: {}", apkH.getName());
                fpService.getPackageHierarchiesByName(apkH.getName())
                    .forEach(libH -> {
                        double score = calculateHybridScore(apkH, libH);
                        LOGGER.debug("{} -> {} : {}", apkH.getName(), libH.getName(), FRMT.format(score));
                        if(score == 0.0d) {
                            LOGGER.warn("*** NEXT {}: self db match is 0.0!!", apkH.getName());
                            LOGGER.debug(apkH.toString());
                            LOGGER.debug(libH.toString());
                        }
                    });
            }
        }
    }
}
