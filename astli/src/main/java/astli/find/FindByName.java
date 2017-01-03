package astli.find;

import astli.db.EntityService;
import astli.db.Package;
import astli.pojo.PackageHierarchy;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Stream;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class FindByName implements CandidateFinder {

    private final EntityService service;

    public FindByName(EntityService service) {
        this.service = service;
    }
    
    @Override
    public Stream<Package> findCandidates(PackageHierarchy pckg) {
        try {
            List<Package> packages = service.findPackagesByName(pckg.getName());
            return packages.stream();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
    
}
