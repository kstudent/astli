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
    private final int minNeedleParticularity;
    private final int maxNeedleAmount;

    public ParticularCandidateFinder(EntityService service, int minNeedleParticularity, int maxNeedleAmount) {
        this.service = service;
        this.minNeedleParticularity = minNeedleParticularity;
        this.maxNeedleAmount = maxNeedleAmount;
    }
    
    @Override
    public Stream<Package> findCandidates(PackageHierarchy pckg) {
        return distillMethodsWithHighParticularity(pckg)
                .limit(maxNeedleAmount)
                .flatMap(needle -> findCandidateBy(needle))
                .distinct();
    }
    
    private Stream<Fingerprint> distillMethodsWithHighParticularity(PackageHierarchy hierarchy) {
        return hierarchy.getClassNames().parallelStream()
                .map(name -> hierarchy.getMethodsByClassName(name))
                .flatMap(methods -> methods.values().stream())
                .filter(method -> method.getParticularity() > minNeedleParticularity)
                .sorted((that, othr) -> (-1) * Integer.compare(that.getParticularity(), othr.getParticularity()));
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
