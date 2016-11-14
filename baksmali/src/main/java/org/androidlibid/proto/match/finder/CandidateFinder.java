package org.androidlibid.proto.match.finder;

import java.util.stream.Stream;
import org.androidlibid.proto.PackageHierarchy;
import org.androidlibid.proto.ao.Package;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public interface CandidateFinder {
    Stream<Package> findCandidates(PackageHierarchy pckg);
}
