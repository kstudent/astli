//package org.androidlibid.proto.ao;
//
//import java.sql.SQLException;
//import org.androidlibid.proto.Fingerprint;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//
///**
// *
// * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
// */
//public class LibraryFingerprintDBUpdater {
//    
//    private final EntityService service;
//    private static final Logger LOGGER = LogManager.getLogger(LibraryFingerprintDBUpdater.class);
//
//    public LibraryFingerprintDBUpdater(EntityService service) {
//        this.service = service;
//    }
//    
//    public void update(String libname) throws SQLException {
//        
//        Library lib = service.findLibraryByMvnIdentifier(libname);
//        
//        if(lib == null) {
//            LOGGER.warn("The Library " + libname + " could not be found.");
//            return;
//        }
//            
//        Fingerprint libFingerprint = new Fingerprint(lib);  
//        
//        if(libFingerprint.getLength() > 0.0d) {
//            LOGGER.warn("The Library " + libname + " could not be found.");
//            return;
//        }
//
//        for(Package pckg : lib.getPackages()) {
//            Fingerprint pckgFingerprint = new Fingerprint(pckg);
//
//            for(Clazz clazz : pckg.getClazzes()) {
//                Fingerprint clazzFingerprint = new Fingerprint(clazz);
//
//                pckgFingerprint.sumFeatures(clazzFingerprint);
//            }
//
//            pckg.setVector(pckgFingerprint.getFeatureVector().toBinary());
//            pckg.save();
//
//            libFingerprint.sumFeatures(pckgFingerprint);
//        }
//
//        lib.setVector(libFingerprint.getFeatureVector().toBinary());
//        lib.save();
//    }
//    
//}
