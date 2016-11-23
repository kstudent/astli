package astli.score;

import astli.pojo.PackageHierarchy;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class InclusionMatcher implements PackageMatcher {

    private final InclusionChecker checker = new InclusionChecker(new HungarianAlgorithm());
    
    @Override
    public double getScore(PackageHierarchy a, PackageHierarchy b) {
        InclusionChecker.Result result = checker.checkInclusion(a, b);
        return result.packageAIsIncludedInB() ? 0.0 : 1.0;
    }
}
