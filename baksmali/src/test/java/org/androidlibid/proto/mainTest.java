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
        String appApkPath = "./src/test/resources/FingerprintAPKTest/app.apk";
        File appApk = new File(appApkPath);
        assert(appApk.exists() && appApk.canRead());
        
        String arg[] = {"-y", appApkPath};
        main.main(arg); 
    }
}
