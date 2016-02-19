package org.androidlibid.proto.integration;

import java.io.File;
import java.io.IOException;
import org.jf.baksmali.main;
import org.androidlibid.proto.Fingerprint;
import org.androidlibid.proto.ao.EntityService;
import org.androidlibid.proto.ao.EntityServiceFactory;
import org.junit.Test;
import org.androidlibid.proto.ao.Class;


/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class mainTest {
    
    @Test
    public void storeLibandMatchAPK() throws Exception {
        clearDB();
        testFingerprintLibraries();
        testListPrintFingerprintsFromDB();
        testFingerprintApplication();
    }
    
    public void testFingerprintApplication() throws IOException {
        String appApkPath = "./src/integration-test/resources/FingerprintAPKTest/app.apk";
        File appApk = new File(appApkPath);
        assert(appApk.exists() && appApk.canRead());
        
        String arg[] = {"-y", appApkPath};
        main.main(arg); 
    }
    
    public void testFingerprintLibraries() throws IOException {
        String jarPath = "./src/integration-test/resources/FingerprintJARTest/lib_spongy_core.jar";
        File jarFile = new File(jarPath);
        assert(jarFile.exists() && jarFile.canRead());
        String arg[] = {"-z", "com.madgag.spongycastle:core:1.54.0.0", jarPath};
        main.main(arg); 
        jarPath = "./src/integration-test/resources/FingerprintJARTest/lib_spongy_prov.jar";
        jarFile = new File(jarPath);
        assert(jarFile.exists() && jarFile.canRead());
        String arg2[] = {"-z", "com.madgag.spongycastle:prov:1.54.0.0", jarPath};
        main.main(arg2); 
    }
    
    public void clearDB() throws Exception {
        EntityService service = EntityServiceFactory.createService();
        System.out.println("Fingerprint.count(): " + service.countClassFingerprints());
        service.truncateTables();
        System.out.println("... after deleting : " + service.countClassFingerprints());
    } 
    
    public void testListPrintFingerprintsFromDB() throws Exception {
        EntityService service = EntityServiceFactory.createService();
        
        int counter = 0;
        
        System.out.println("---list-of-fingerprints---");
        for(Class entity : service.getClassFingerprintEntities()) {
            
            assert(entity != null);
            assert(entity.getVector() != null);
            
            Fingerprint print = new Fingerprint(entity);
            counter++;
            System.out.println("  " + print.getName());
        }
        
        System.out.println("amount of fingerprints: " + counter);
        System.out.println("---end testPrintFingerprintsFromDB---");
    }
    
}
