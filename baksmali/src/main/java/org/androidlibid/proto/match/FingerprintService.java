package org.androidlibid.proto.match;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.androidlibid.proto.Fingerprint;
import org.androidlibid.proto.ao.Clazz;
import org.androidlibid.proto.ao.EntityService;
import org.androidlibid.proto.ao.Method;
import org.androidlibid.proto.ao.Package;

/**
 * Convenient Facade of EntityService to hide AO layer
 * 
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class FingerprintService {

    private final EntityService service; 

    FingerprintService(EntityService service) {
        this.service = service;
    }

    public List<Fingerprint> findMethodsByPackageDepth(int depth) throws SQLException {
        
        List<Fingerprint> haystack = new ArrayList<>();
        
        for (Package pckg : service.findPackagesByDepth(depth)) {
            Fingerprint pckgFingerprint = new Fingerprint(pckg);
            
            for(Clazz classEntity : pckg.getClasses()) {
                Fingerprint classFingerprint = new Fingerprint(classEntity);
                pckgFingerprint.addChild(classFingerprint);
                
                for (Method methodEntity : classEntity.getMethods()) {
                    Fingerprint methodFingerprint = new Fingerprint(methodEntity);
                    classFingerprint.addChild(methodFingerprint);

                    haystack.add(methodFingerprint);
                }
            }
        }
        
        return haystack; 
    }
}
