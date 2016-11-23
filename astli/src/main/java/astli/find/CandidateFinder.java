package astli.find;

import java.util.stream.Stream;
import astli.pojo.PackageHierarchy;
import astli.db.Package;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public interface CandidateFinder {
    Stream<Package> findCandidates(PackageHierarchy pckg);
}
