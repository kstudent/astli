package org.androidlibid.proto.integration;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import org.jf.baksmali.main;
import org.androidlibid.proto.Fingerprint;
import org.androidlibid.proto.ao.EntityService;
import org.androidlibid.proto.ao.EntityServiceFactory;
import org.junit.Test;
import org.androidlibid.proto.ao.Class;
import org.androidlibid.proto.ao.Package;


/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class mainTest {
    
    @Test
    public void storeLibandMatchAPK() throws Exception {
        clearDB();
        testStoreFingerprintLibSpongyCore();
        testStoreFingerprintLibSpongyProv();
        testFindLibrariesOfAPK();
    }
    
    @Test
    public void storeLibandMatchAPKLvl() throws Exception {
        clearDB();
        testStoreFingerprintLibSpongyCore();
        testStoreFingerprintLibSpongyProv();
        testFindLibrariesOfAPKLvl(1);
    }
    
    public void testFindLibrariesOfAPK() throws IOException {
        String appApkPath = "./src/integration-test/resources/FingerprintAPKTest/app.apk";
        File appApk = new File(appApkPath);
        assert(appApk.exists() && appApk.canRead());
        
        String arg[] = {"-y", appApkPath};
        main.main(arg); 
    }

    public void testFindLibrariesOfAPKLvl(int lvl) throws IOException {
        String appApkPath     =  "./src/integration-test/resources/FingerprintAPKTest/app.obflvl" + lvl + ".apk";
        String mappingFilePath = "./src/integration-test/resources/MappingFiles/mapping.obflvl"   + lvl + ".txt";
        
        File appApk = new File(appApkPath);
        assert(appApk.exists() && appApk.canRead());
        File mappingFile = new File(mappingFilePath);
        assert(mappingFile.exists() && mappingFile.canRead());
        
        String arg[] = {"-y", appApkPath, "-Z", mappingFilePath};
        main.main(arg); 
    }
    
    public void testStoreFingerprintLibSpongyCore() throws IOException {
        String jarPath = "./src/integration-test/resources/FingerprintJARTest/lib_spongy_core.jar";
        File jarFile = new File(jarPath);
        assert(jarFile.exists() && jarFile.canRead());
        String arg[] = {"-z", "com.madgag.spongycastle:core:1.54.0.0", jarPath};
        main.main(arg); 
    }

    public void testStoreFingerprintLibSpongyProv() throws IOException {
        String jarPath = "./src/integration-test/resources/FingerprintJARTest/lib_spongy_prov.jar";
        File jarFile = new File(jarPath);
        assert(jarFile.exists() && jarFile.canRead());
        String arg[] = {"-z", "com.madgag.spongycastle:prov:1.54.0.0", jarPath};
        main.main(arg); 
    }
    
    public void clearDB() throws Exception {
        EntityService service = EntityServiceFactory.createService();
        System.out.println("Fingerprint.count(): " + service.countClasses());
        service.truncateTables();
        System.out.println("... after deleting : " + service.countClasses());
    } 
    
    public void testListClassFingerprintsFromDB() throws Exception {
        EntityService service = EntityServiceFactory.createService();
        
        int counter = 0;
        
        System.out.println("---list-of-class-fingerprints---");
        for(Class entity : service.findClasses()) {
            
            assert(entity != null);
            assert(entity.getVector() != null);
            
            Fingerprint print = new Fingerprint(entity);
            counter++;
            System.out.println("  " + print.getName());
        }
        
        System.out.println("amount of classes: " + counter);
    }

    private void testListPackageFingerprintsFromDB() throws SQLException {
        EntityService service = EntityServiceFactory.createService();
        
        int counter = 0;
        
        System.out.println("---list-of-package-fingerprints---");
        for(Package entity : service.findPackages()) {
            
            assert(entity != null);
            assert(entity.getVector() != null);
            
            Fingerprint print = new Fingerprint(entity);
            counter++;
            System.out.println("  " + print.getName());
        }
        
        System.out.println("amount of packages: " + counter);
    }
    
}
