package org.androidlibid.proto.integration;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import org.jf.baksmali.main;
import org.androidlibid.proto.Fingerprint;
import org.androidlibid.proto.ao.EntityService;
import org.androidlibid.proto.ao.EntityServiceFactory;
import org.junit.Test;
import org.androidlibid.proto.ao.Package;
import org.androidlibid.proto.ao.Clazz;
import org.androidlibid.proto.ao.Method;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class mainTest {
    
    @Test
    public void storeLibs() throws Exception {
        clearDB();
        testStoreFingerprintLibSpongyCore();
        testStoreFingerprintLibSpongyProv();
        countMethods();
    }
    
    @Test
    public void matchAPKLvl1() throws Exception {
        testFindLibrariesOfAPKLvl(1);
    }
    @Test
    public void matchAPKLvl2() throws Exception {
        testFindLibrariesOfAPKLvl(2);
    }
    @Test
    public void matchAPKLvl3() throws Exception {
        testFindLibrariesOfAPKLvl(3);
    }
    
    public void testFindLibrariesOfAPK() throws IOException {
        String appApkPath = "./src/integration-test/resources/FingerprintAPKTest/app.apk";
        File appApk = new File(appApkPath);
            assert(appApk.exists() && appApk.canRead());
        
        String arg[] = {"-y", appApkPath};
        main.main(arg); 
    }

    public void testFindLibrariesOfAPKLvl(int lvl) throws IOException {
        System.out.println("analyzing lvl " + lvl);
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
    
     public void countMethods() throws Exception {
        EntityService service = EntityServiceFactory.createService();
        System.out.println("methods: " + service.countMethods());
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
        for(Clazz entity : service.findClasses()) {
            
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

    private void printSomeMethods() throws SQLException {
      
        EntityService service = EntityServiceFactory.createService();
        List<Clazz> classes = service.findClasses();
        int upper_bound = (classes.size() > 10) ? 10 : classes.size();
        
        for(int i = 0; i < upper_bound; i++) {
            Clazz clazz = classes.get(i);
            System.out.println(clazz.getName());
            for(Method m : clazz.getMethods()) {
                Fingerprint p = new Fingerprint(m);
                System.out.println(p);  
            }
        }
    }
    
    private void printMethodLengths() throws SQLException {
      
        EntityService service = EntityServiceFactory.createService();
        List<Clazz> classes = service.findClasses();
        
        for(int i = 0; i < classes.size(); i++) {
            Clazz clazz = classes.get(i);
            for(Method m : clazz.getMethods()) {
                System.out.print(m.getLength() + ", ");
            }
        }
    }
}
