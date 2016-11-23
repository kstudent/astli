package astli.score;

import astli.pojo.PackageHierarchy;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public interface PackageMatcher {

    double getScore(PackageHierarchy a, PackageHierarchy b);
    
}
