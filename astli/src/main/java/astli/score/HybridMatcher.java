package astli.score;

import astli.pojo.PackageHierarchy;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class HybridMatcher implements PackageMatcher {

    private final InclusionChecker checker = new InclusionChecker(new HungarianAlgorithm());
    private final SimilarityMatcher simMatcher = new SimilarityMatcher(new HungarianAlgorithm());
    
    @Override
    public double getScore(PackageHierarchy a, PackageHierarchy b) {
        InclusionChecker.Result result = checker.checkInclusion(a, b);
        
        if(result.packageAIsIncludedInB()) {
            return simMatcher.getScore(a, b);
        } else {
            return 0.0d;
        }
    }
    
    
}
