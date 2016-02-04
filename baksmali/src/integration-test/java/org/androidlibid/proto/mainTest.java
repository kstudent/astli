package org.androidlibid.proto;

import java.io.File;
import java.io.IOException;
import org.jf.baksmali.main;
import org.junit.Test;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class mainTest {
    
    @Test
    public void testFingerprintAPK() throws IOException {
        String appApkPath = "./src/integration-test/resources/FingerprintAPKTest/app.apk";
        File appApk = new File(appApkPath);
        assert(appApk.exists() && appApk.canRead());
        
        String arg[] = {"-y", appApkPath};
        main.main(arg); 
    }
    
    @Test
    public void testFingerprintJAR() throws IOException {
        String jarPath = "./src/integration-test/resources/FingerprintJARTest/lib.jar";
        File jarFile = new File(jarPath);
        assert(jarFile.exists() && jarFile.canRead());
        String arg[] = {"-z", jarPath};
        main.main(arg); 
    }
}
