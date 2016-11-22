package org.androidlibid.proto.match.matcher;

import org.androidlibid.proto.pojo.PackageHierarchy;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public interface PackageMatcher {

    double getScore(PackageHierarchy a, PackageHierarchy b);
    
}
