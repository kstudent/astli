package org.androidlibid.proto.ao;

import java.util.Collection;
import org.androidlibid.proto.PackageHierarchy;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class PackageHierarchyService {
    
    private final EntityService service; 

    public PackageHierarchyService(EntityService service) {
        this.service = service;
    }

    public void saveHierarchies(Collection<PackageHierarchy> values) {
        //TODO
    }
    
    public void saveHierarchy(PackageHierarchy hierarchy) {
        //TODO
    }
}
