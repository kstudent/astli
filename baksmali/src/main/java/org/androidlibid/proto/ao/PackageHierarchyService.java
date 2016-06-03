package org.androidlibid.proto.ao;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import org.androidlibid.proto.Fingerprint;
import org.androidlibid.proto.PackageHierarchy;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class PackageHierarchyService {
    
    private final EntityService service;
    private final String libName;

    public PackageHierarchyService(EntityService service, String libName) {
        this.service = service;
        this.libName = libName;
    }
    
    public void saveHierarchies(Collection<PackageHierarchy> hierarchies) throws SQLException {
        for (PackageHierarchy hierarchy : hierarchies) {
            saveHierarchy(hierarchy);
        } 
    }
    
    public void saveHierarchy(PackageHierarchy hierarchy) throws SQLException {
        String packageName = hierarchy.getName();
        
        for (String className : hierarchy.getClassNames()) {
            
            Clazz clazz = service.saveClass(className, packageName, libName);
            
            Map<String, Fingerprint> methods = hierarchy.getMethodsByClassName(className);
            
            for (String methodName : methods.keySet()) {
                
                Fingerprint methodPrint = methods.get(methodName);
                
                service.saveMethod(methodPrint.getBinaryFeatureVector(), 
                        methodName, methodPrint.getSignature(), clazz);
                
            }
        }
    }
}
