package org.androidlibid.proto.integration;

import com.android.dex.util.FileUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.jf.baksmali.main;
import org.junit.Assert;
import org.junit.Test;
import org.androidlibid.proto.Fingerprint;
import org.androidlibid.proto.ao.ClassEntity;
import org.androidlibid.proto.ao.ClassEntityService;
import org.androidlibid.proto.ao.ClassEntityServiceFactory;
import org.androidlibid.proto.ao.JdbcProperties;
import org.junit.Test;


/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class mainTest {
    
    @Test
    public void storeLibandMatchAPK() throws Exception {
        clearDB();
        testFingerprintLibraries();
        testPrintFingerprintsFromDB();
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
        String arg[] = {"-z", jarPath};
        main.main(arg); 
        jarPath = "./src/integration-test/resources/FingerprintJARTest/lib_spongy_prov.jar";
        jarFile = new File(jarPath);
        assert(jarFile.exists() && jarFile.canRead());
        String arg2[] = {"-z", jarPath};
        main.main(arg2); 
    }
    
    public void clearDB() throws Exception {
        ClassEntityService service = ClassEntityServiceFactory.createService();
        System.out.println("Fingerprint.count(): " + service.countFingerprints());
        service.deleteAllFingerprints();
        System.out.println("... after deleting : " + service.countFingerprints());
    } 
    
    
    public void testPrintFingerprintsFromDB() throws Exception {
        ClassEntityService service = ClassEntityServiceFactory.createService();
        
        int counter = 0;
        
        System.out.println("---list-of-fingerprints---");
        for(ClassEntity entity : service.getFingerprintEntities()) {
            Fingerprint print = new Fingerprint(entity);
            counter++;
            System.out.println("  " + print.getName());
        }
        
        System.out.println("amount of fingerprints: " + counter);
        System.out.println("---end testPrintFingerprintsFromDB---");
    }
    
}
