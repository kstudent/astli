package astli.find;

import java.util.stream.Stream;
import astli.pojo.Fingerprint;
import astli.pojo.PackageHierarchy;
import astli.db.EntityService;
import astli.db.Package;
import java.sql.SQLException;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class ParticularCandidateFinder implements CandidateFinder {

    private final EntityService service;
    private final int minimalNeedleEntropy;
    private final int needleAmount;

    public ParticularCandidateFinder(EntityService service, int minimalNeedleEntropy, int needleAmount) {
        this.service = service;
        this.minimalNeedleEntropy = minimalNeedleEntropy;
        this.needleAmount = needleAmount;
    }
    
    @Override
    public Stream<Package> findCandidates(PackageHierarchy pckg) {
        return distillMethodsWithHighEntropy(pckg)
                .limit(needleAmount)
                .flatMap(needle -> findCandidateBy(needle))
                .distinct();
    }
    
    private Stream<Fingerprint> distillMethodsWithHighEntropy(PackageHierarchy hierarchy) {
        return hierarchy.getClassNames().parallelStream()
                .map(name -> hierarchy.getMethodsByClassName(name))
                .flatMap(methods -> methods.values().stream())
                .filter(method -> method.getEntropy() > minimalNeedleEntropy)
                .sorted((that, othr) -> (-1) * Integer.compare(that.getEntropy(), othr.getEntropy()));
    }

    private Stream<Package> findCandidateBy(Fingerprint needle) {
        try {
            return service
                .findPackageCandidateBySignatureAndVector(needle.getSignature(), needle.getBinaryFeatureVector())
                .stream();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
    
}
