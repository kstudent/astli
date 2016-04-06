package org.androidlibid.proto.ao;

import java.sql.SQLException;
import org.androidlibid.proto.Fingerprint;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class LibraryFingerprintDBUpdater {
    
    private final EntityService service;

    public LibraryFingerprintDBUpdater(EntityService service) {
        this.service = service;
    }
    
    public void update(String libname) throws SQLException {
        
        Library lib = service.findLibraryByMvnIdentifier(libname);
        
        if(lib == null) {
            throw new RuntimeException("The Library " + libname + " could not be found.");
        }
            
        Fingerprint libFingerprint = new Fingerprint(lib);  
        
        if(libFingerprint.getLength() > 0.0d) {
            throw new RuntimeException("The Library " + libname + " already has a fingerprint.");
        }

        for(Package pckg : lib.getPackages()) {
            Fingerprint pckgFingerprint = new Fingerprint(pckg);

            for(Clazz clazz : pckg.getClazzes()) {
                Fingerprint clazzFingerprint = new Fingerprint(clazz);

                pckgFingerprint.sumFeatures(clazzFingerprint);
            }

            pckg.setVector(pckgFingerprint.getFeatureVector().toBinary());
            pckg.save();

            libFingerprint.sumFeatures(pckgFingerprint);
        }

        lib.setVector(libFingerprint.getFeatureVector().toBinary());
        lib.save();
    }
    
}
