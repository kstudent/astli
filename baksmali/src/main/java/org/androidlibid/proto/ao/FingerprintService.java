package org.androidlibid.proto.ao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.androidlibid.proto.Fingerprint;
import org.androidlibid.proto.PackageHierarchy;

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
    
    public List<Fingerprint> findSameMethods(Fingerprint needle) throws SQLException {
        return service
                .findMethodsBySignatureAndVector(needle.getSignature(), needle.getBinaryFeatureVector())
                .stream()
                .map(e -> new Fingerprint(e))
                .collect(Collectors.toList());
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
    
    public PackageHierarchy getPackageHierarchyByFingerprint(Fingerprint print) {
        
        FingerprintEntity printEntity = print.getMethod();

        Package pckg = printEntity.getClazz().getPackage();
        
        return createHierarchyFromPackage(pckg); 
    }
    
    public Stream<PackageHierarchy> getPackageHierarchies() throws SQLException {
        return service.findPackages().stream()
                .map(pckg -> createHierarchyFromPackage(pckg));
    }    

    private PackageHierarchy createHierarchyFromPackage(Package pckg) {
        PackageHierarchy hierarchy = new PackageHierarchy(pckg.getName());
        
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
