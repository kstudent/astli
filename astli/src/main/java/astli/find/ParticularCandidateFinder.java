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
    private final int minimalNeedleEntropy = 12;

    public ParticularCandidateFinder(EntityService service) {
        this.service = service;
    }
    
    @Override
    public Stream<Package> findCandidates(PackageHierarchy pckg) {
        return distillMethodsWithHighEntropy(pckg)
                .limit(10)
                .flatMap(needle -> findCandidateBy(needle))
                .distinct();
    }
    
    Stream<Fingerprint> distillMethodsWithHighEntropy(PackageHierarchy hierarchy) {
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
