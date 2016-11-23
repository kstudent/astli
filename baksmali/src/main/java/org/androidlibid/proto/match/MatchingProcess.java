package org.androidlibid.proto.match;

import org.androidlibid.proto.pojo.Match;
import org.androidlibid.proto.match.matcher.PackageMatcher;
import org.androidlibid.proto.match.finder.CandidateFinder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.androidlibid.proto.pojo.PackageHierarchy;
import org.androidlibid.proto.ao.FingerprintService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.androidlibid.proto.ao.Package;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class MatchingProcess implements Function<PackageHierarchy, Match> {

    private static final Logger LOGGER = LogManager.getLogger(MatchingProcess.class);
    private static final NumberFormat FRMT = new DecimalFormat("#0.00");
    private final Map<Package, PackageHierarchy> hierarchyCache;

    private final FingerprintService fpService;
    private final PackageMatcher matcher;
    private final CandidateFinder finder;
    
    /**
     *
     * @param fpService
     * @param matcher
     * @param finder
     */
    public MatchingProcess(FingerprintService fpService, PackageMatcher matcher, CandidateFinder finder) {
        this.fpService = fpService;
        this.matcher = matcher;
        this.hierarchyCache = new HashMap<>();
        this.finder = finder;
    }

    @Override
    public Match apply(PackageHierarchy apkH) {

        final double maxScore =  matcher.getScore(apkH, apkH);

        Stream<Package> candidates = finder.findCandidates(apkH);

        List<Match.Item> results = candidates
                .map(pckg -> getPackageHierarchyCandidate(pckg))
                .map(libH -> {
                    double score = matcher.getScore(apkH, libH) / maxScore;
                    return new Match.Item(score, libH);
                })
                .peek(item -> LOGGER.debug(
                        "{} -> {}: {}", apkH.getName(), item.getPackage(), 
                        FRMT.format(item.getScore())))
                .sorted((that, other) -> Double.compare(other.getScore(), that.getScore()))
                .collect(Collectors.toList());
        
        return new Match(results, apkH, fpService.isPackageInDB(apkH.getName()));
        
    }
    
    private synchronized PackageHierarchy getPackageHierarchyCandidate(Package pckg) {
        if (!hierarchyCache.containsKey(pckg)) {
            PackageHierarchy hierarchy = fpService.createHierarchyFromPackage(pckg);
            hierarchyCache.put(pckg, hierarchy);
        }
        
        return hierarchyCache.get(pckg);
    }
}
