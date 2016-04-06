package org.androidlibid.proto.ao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.androidlibid.proto.Fingerprint;
import org.androidlibid.proto.SmaliNameConverter;

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

    public List<Fingerprint> findMethodsByPackageDepth(int depth) throws SQLException {
        
        List<Fingerprint> haystack = new ArrayList<>();
        
        for (Package pckg : service.findPackagesByDepth(depth)) {
            Fingerprint pckgFingerprint = new Fingerprint(pckg);
            
            for(Clazz classEntity : pckg.getClazzes()) {
                Fingerprint classFingerprint = new Fingerprint(classEntity);
                pckgFingerprint.addChildFingerprint(classFingerprint);
                
                for (Method methodEntity : classEntity.getMethods()) {
                    Fingerprint methodFingerprint = new Fingerprint(methodEntity);
                    classFingerprint.addChildFingerprint(methodFingerprint);

                    haystack.add(methodFingerprint);
                }
            }
        }
        
        return haystack; 
    }
    
    public List<Fingerprint> findMethodsByLength(double length, double size) throws SQLException {
        
        List<Fingerprint> methods = new ArrayList<>();
        
        for(Method methodEntity : service.findMethodsByLength(length, size)) {
            methods.add(new Fingerprint(methodEntity));
        }
        
        return methods;
    }
    
    public Fingerprint getPackageHierarchyByMethod(Fingerprint keyMethod) {
        
        Method keyMethodEntity = (Method) keyMethod.getEntity();
        if(keyMethodEntity == null) {
            throw new RuntimeException("Method.getEntity() was null."); 
        }

        Package packageEntity = keyMethodEntity.getClazz().getPackage();
        
        return getPackageHierarchy(new Fingerprint(packageEntity));
    }
    
    public Fingerprint getPackageHierarchy(Fingerprint pckg) {
        
        Package packageEntity = (Package) pckg.getEntity();
        if(packageEntity == null) {
            throw new RuntimeException("Package.getEntity() was null."); 
        }
        
        if(!pckg.getChildFingerprints().isEmpty()) {
            throw new RuntimeException("Package already had children!"); 
        }
        
        for(Clazz clazzEntity : packageEntity.getClazzes()) {
            Fingerprint clazz = new Fingerprint(clazzEntity);
            
            for(Method methodEntity : clazzEntity.getMethods()) {
                Fingerprint method = new Fingerprint(methodEntity);
                clazz.addChildFingerprint(method);
            }
            pckg.addChildFingerprint(clazz);
        }
        
        return pckg;
    }
    
    public List<Fingerprint> findPackageByName(String name) throws SQLException{
        List<Fingerprint> pckgFingerprints = new ArrayList<>();
        
        for(Package pckg : service.findPackagesByName(name)) {
            pckgFingerprints.add(new Fingerprint(pckg));
        }
                
        return pckgFingerprints;
    }
    
    public List<Fingerprint> findPackages() throws SQLException {
        
        List<Fingerprint> pckgFingerprints = new ArrayList<>();
        
        for(Package pckg : service.findPackages()) {
            pckgFingerprints.add(new Fingerprint(pckg));
        }
                
        return pckgFingerprints;
    }

    public void saveClass(Fingerprint classFingerprint, String mvnIdentifier) throws SQLException {
        
        String className = classFingerprint.getName();
        String packageName = SmaliNameConverter.extractPackageNameFromClassName(className);
        
        Clazz clazz = service.saveClass(
                classFingerprint.getFeatureVector().toBinary(), 
                className, 
                packageName, 
                mvnIdentifier
        );

        for(Fingerprint method : classFingerprint.getChildFingerprints()) {
            service.saveMethod(
                    method.getFeatureVector().toBinary(), 
                    method.getName(), 
                    method.getLength(), 
                    clazz
            );
        }
    }
    
    
}
