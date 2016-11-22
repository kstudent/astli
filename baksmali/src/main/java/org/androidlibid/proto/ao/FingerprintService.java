package org.androidlibid.proto.ao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.androidlibid.proto.pojo.Fingerprint;
import org.androidlibid.proto.pojo.PackageHierarchy;

/**
 * Convenient Facade of EntityService to hide AO layer
 * 
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class FingerprintService {

    private final EntityService service; 
    

    public FingerprintService(EntityService service) {
        this.service = service;
    }
    
    public List<Fingerprint> findMethodsBySignature(String signature) throws SQLException {
        List<Fingerprint> methods = new ArrayList<>();
        
        for(FingerprintEntity methodEntity : service.findMethodsBySignature(signature)) {
            methods.add(new Fingerprint(methodEntity));
        }
        
        return methods;
    }
    
    public Stream<Fingerprint> findSameMethods(Fingerprint needle) {
        try {
            return service
                    .findMethodsBySignatureAndVector(needle.getSignature(), needle.getBinaryFeatureVector())
                    .parallelStream()
                    .map(e -> new Fingerprint(e));
            } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public Stream<Package> findPackagesWithSameMethods(Fingerprint needle) {
        try {
            return service
                    .findPackageCandidateBySignatureAndVector(needle.getSignature(), needle.getBinaryFeatureVector())
                    .stream();
            } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public String getClassNameByFingerprint(Fingerprint print) {
        
        FingerprintEntity keyMethodEntity = print.getMethod();

        Clazz clazzEntity = keyMethodEntity.getClazz(); 
        
        return clazzEntity.getName();
        
    }
    
    public String getPackageNameByFingerprint(Fingerprint print) {
        
        FingerprintEntity keyMethodEntity = print.getMethod();

        Package pckg = keyMethodEntity.getClazz().getPackage(); 
        
        return pckg.getName();
    }
    
    public Stream<PackageHierarchy> getPackageHierarchiesByName(String packageName) {
        try {
            return service.findPackagesByName(packageName).parallelStream()
                    .map(pckg -> createHierarchyFromPackage(pckg));
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public PackageHierarchy getPackageHierarchyByFingerprint(Fingerprint print) {
        
        FingerprintEntity printEntity = print.getMethod();

        Package pckg = printEntity.getClazz().getPackage();
        
        return createHierarchyFromPackage(pckg); 
    }
    
    public Stream<PackageHierarchy> getPackageHierarchies() throws SQLException {
        return service.findPackages().parallelStream()
                .map(pckg -> createHierarchyFromPackage(pckg));
    }
    
    public boolean isPackageInDB(String packageName) {
        try {
            return !service.findPackagesByName(packageName).isEmpty();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public PackageHierarchy createHierarchyFromPackage(Package pckg) {
        PackageHierarchy hierarchy = new PackageHierarchy(pckg.getName(),
                pckg.getLibrary().getName());
        
        for(Clazz clazz : pckg.getClazzes()) {
            
            Map<String, Fingerprint> methods = new HashMap<>();
            
            for(FingerprintEntity method : clazz.getMethods()) {
                methods.put(method.getName(), new Fingerprint(method));
            }
            
            hierarchy.addMethods(clazz.getName(), methods);
        }
        
        return hierarchy;
    }
}
