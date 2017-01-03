package astli.find;

import astli.db.Package;
import astli.pojo.PackageHierarchy;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class FindByNameOrNeedle implements CandidateFinder {

    private final FindByName nameFinder; 
    private final FindByNeedle needleFinder; 

    public FindByNameOrNeedle(FindByName nameFinder, FindByNeedle needleFinder) {
        this.nameFinder = nameFinder;
        this.needleFinder = needleFinder;
    }
    
    @Override
    public Stream<Package> findCandidates(PackageHierarchy pckg) {
        List<Package> candidateList = nameFinder.findCandidates(pckg)
                .collect(Collectors.toList());
        
        if(candidateList.isEmpty()) {
            return needleFinder.findCandidates(pckg);
        }
        
        return candidateList.stream();
    }
    
}
