package org.androidlibid.proto;

import com.android.dex.util.FileUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.jf.baksmali.main;
import org.junit.Assert;
import org.junit.Test;
import org.androidlibid.proto.ao.FingerprintService;
import org.androidlibid.proto.ao.JdbcProperties;
import org.androidlibid.proto.ao.FingerprintService;
import org.androidlibid.proto.ao.FingerprintServiceFactory;
import org.junit.Test;


/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class mainTest {
    
    @Test
    public void storeLibandMatchAPK() throws Exception {
        clearDB();
        testFingerprintLibrary();
        testFingerprintApplication();
    }
    
    public void testFingerprintApplication() throws IOException {
        String appApkPath = "./src/integration-test/resources/FingerprintAPKTest/app.apk";
        File appApk = new File(appApkPath);
        assert(appApk.exists() && appApk.canRead());
        
        String arg[] = {"-y", appApkPath};
        main.main(arg); 
    }
    
    public void testFingerprintLibrary() throws IOException {
        String jarPath = "./src/integration-test/resources/FingerprintJARTest/lib.jar";
        File jarFile = new File(jarPath);
        assert(jarFile.exists() && jarFile.canRead());
        String arg[] = {"-z", jarPath};
        main.main(arg); 
    }
    
    void clearDB() throws Exception {
        FingerprintService service = FingerprintServiceFactory.createService();
        System.out.println("Fingerprint.count(): " + service.countFingerprints());
        service.deleteAllFingerprints();
        System.out.println("... after deleting : " + service.countFingerprints());
    } 
    
}
