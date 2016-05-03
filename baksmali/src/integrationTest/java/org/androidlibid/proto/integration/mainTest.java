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
import org.androidlibid.proto.match.SetupLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jf.baksmali.baksmaliOptions;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class mainTest {
   
    private static final Logger LOGGER = LogManager.getLogger(mainTest.class);
    
    private static final String resourcesSrcDir = "./src/integrationTest/resources/";
    
    @Test
    public void storeLibs() throws Exception {
        clearDB();
        testStoreFingerprintLibSpongyCore();
        testStoreFingerprintLibSpongyProv();
        countMethods();
    }
    
    @Test
    public void matchAPKLvl1Alg1() throws Exception {
        testFindLibrariesOfAPKLvl(1, 1);
    }
    
    @Test
    public void matchAPKLvl2Alg1() throws Exception {
        testFindLibrariesOfAPKLvl(2, 1);
    }
    
    @Test
    public void matchAPKLvl3Alg1() throws Exception {
        testFindLibrariesOfAPKLvl(3, 1);
    }
    
    @Test
    public void matchAPKLvl1Alg2() throws Exception {
        testFindLibrariesOfAPKLvl(1, 2);
    }
    
    @Test
    public void matchAPKLvl2Alg2() throws Exception {
        testFindLibrariesOfAPKLvl(2, 2);
    }
    
    @Test
    public void matchAPKLvl3Alg2() throws Exception {
        testFindLibrariesOfAPKLvl(3, 2);
    }
    
    @Test
    public void printMethodsOfPackage() throws SQLException {
      
        String packageName = "org.spongycastle.pqc.math.linearalgebra"; 
        EntityService service = EntityServiceFactory.createService();
        
        List<Package> packages = service.findPackagesByName(packageName);
        
        for(Package pckg : packages) {
            for(Clazz clazz : pckg.getClazzes()) {
                LOGGER.info("* " + clazz.getName());
                for(Method m : clazz.getMethods()) {
                    LOGGER.info("** " + m.getName());
                    Fingerprint p = new Fingerprint(m);
                    LOGGER.info(p);
                }
            }
        }
    }
    
    
    public void testFindLibrariesOfAPK() throws IOException {
        String appApkPath = resourcesSrcDir + "FingerprintAPKTest/app.apk";
        File appApk = new File(appApkPath);
            assert(appApk.exists() && appApk.canRead());
        
        String arg[] = {"-y", appApkPath};
        main.main(arg); 
    }

    public void testFindLibrariesOfAPKLvl(int lvl, int algId) throws IOException {
        LOGGER.info("analyzing lvl " + lvl);
        String appApkPath     =  resourcesSrcDir + "FingerprintAPKTest/app.obflvl" + lvl + ".apk";
        String mappingFilePath = resourcesSrcDir + "MappingFiles/mapping.obflvl"   + lvl + ".txt";
        
        File appApk = new File(appApkPath);
        assert(appApk.exists() && appApk.canRead());
        File mappingFile = new File(mappingFilePath);
        assert(mappingFile.exists() && mappingFile.canRead());
        
        String arg[] = {"-y", appApkPath, "-Z", mappingFilePath, "-a", Integer.toString(algId)};
        main.main(arg); 
    }
    
    public void testStoreFingerprintLibSpongyCore() throws IOException {
        String jarPath = resourcesSrcDir + "FingerprintJARTest/lib_spongy_core.jar";
        File jarFile = new File(jarPath);
        assert(jarFile.exists() && jarFile.canRead());
        String arg[] = {"-z", "com.madgag.spongycastle:core:1.54.0.0", jarPath};
        main.main(arg); 
    }

    public void testStoreFingerprintLibSpongyProv() throws IOException {
        String jarPath = resourcesSrcDir + "FingerprintJARTest/lib_spongy_prov.jar";
        File jarFile = new File(jarPath);
        assert(jarFile.exists() && jarFile.canRead());
        String arg[] = {"-z", "com.madgag.spongycastle:prov:1.54.0.0", jarPath};
        main.main(arg); 
    }
    
     public void countMethods() throws Exception {
        EntityService service = EntityServiceFactory.createService();
        LOGGER.info("methods: " + service.countMethods());
    } 
    
    public void clearDB() throws Exception {
        EntityService service = EntityServiceFactory.createService();
        LOGGER.info("Fingerprint.count(): " + service.countClasses());
        service.truncateTables();
        LOGGER.info("... after deleting : " + service.countClasses());
    } 
    
    public void testListClassFingerprintsFromDB() throws Exception {
        EntityService service = EntityServiceFactory.createService();
        
        int counter = 0;
        
        LOGGER.info("---list-of-class-fingerprints---");
        for(Clazz entity : service.findClasses()) {
            
            assert(entity != null);
            assert(entity.getVector() != null);
            
            Fingerprint print = new Fingerprint(entity);
            counter++;
            LOGGER.info("  " + print.getName());
        }
        
        LOGGER.info("amount of classes: " + counter);
    }
    
    private void testListPackageFingerprintsFromDB() throws SQLException {
        EntityService service = EntityServiceFactory.createService();
        
        int counter = 0;
        
        LOGGER.info("---list-of-package-fingerprints---");
        for(Package entity : service.findPackages()) {
            
            assert(entity != null);
            assert(entity.getVector() != null);
            
            Fingerprint print = new Fingerprint(entity);
            counter++;
            LOGGER.info("  " + print.getName());
        }
        
        LOGGER.info("amount of packages: " + counter);
    }

    @Test
    public void printSetup() throws SQLException {
        new SetupLogger(new baksmaliOptions(), EntityServiceFactory.createService()).logSetup();
    }
    
    private void printSomeMethods() throws SQLException {
      
        EntityService service = EntityServiceFactory.createService();
        List<Clazz> classes = service.findClasses();
        int upper_bound = (classes.size() > 10) ? 10 : classes.size();
        
        for(int i = 0; i < upper_bound; i++) {
            Clazz clazz = classes.get(i);
            LOGGER.info(clazz.getName());
            for(Method m : clazz.getMethods()) {
                Fingerprint p = new Fingerprint(m);
                LOGGER.info(p);  
            }
        }
    }
    
    private void printMethodLengths() throws SQLException {
      
        EntityService service = EntityServiceFactory.createService();
        List<Clazz> classes = service.findClasses();
        
        for(int i = 0; i < classes.size(); i++) {
            Clazz clazz = classes.get(i);
            for(Method m : clazz.getMethods()) {
                LOGGER.info(m.getLength() + ", ");
            }
        }
    }
}
