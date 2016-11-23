package astli.find;

import java.util.stream.Stream;
import astli.pojo.Fingerprint;
import astli.pojo.PackageHierarchy;
import astli.db.FingerprintService;
import astli.db.Package;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class ParticularCandidateFinder implements CandidateFinder {

    private final FingerprintService fpService;
    private final int minimalNeedleEntropy = 12;

    public ParticularCandidateFinder(FingerprintService fpService) {
        this.fpService = fpService;
    }
    
    @Override
    public Stream<Package> findCandidates(PackageHierarchy pckg) {
        return distillMethodsWithHighEntropy(pckg)
                .limit(10)
                .flatMap(needle -> fpService.findPackagesWithSameMethods(needle))
                .distinct();
    }
    
    Stream<Fingerprint> distillMethodsWithHighEntropy(PackageHierarchy hierarchy) {
        return hierarchy.getClassNames().parallelStream()
                .map(name -> hierarchy.getMethodsByClassName(name))
                .flatMap(methods -> methods.values().stream())
                .filter(method -> method.getEntropy() > minimalNeedleEntropy)
                .sorted((that, othr) -> (-1) * Integer.compare(that.getEntropy(), othr.getEntropy()));
    }
    
}
