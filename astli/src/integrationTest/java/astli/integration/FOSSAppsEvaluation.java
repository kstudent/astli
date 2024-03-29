package astli.integration;

import java.io.File;
import java.io.FilenameFilter;
import astli.main.main;
import org.junit.Test;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class FOSSAppsEvaluation {
    
    private static final String resourcesSrcDir = "./src/integrationTest/resources/"; 
    
    private static final FilenameFilter JARFILTER = new FilenameFilter() {
        @Override
        public boolean accept(File file, String string) {
            return string.matches(".*\\.jar");
        }
    };
    
//    @Test
    public void storeLibs() throws Exception {
        mainTest mt = new mainTest();
        
        mt.clearDB();
        
        String libsPath = resourcesSrcDir + "fossEvaluation/libs/";
        File libsFolder = new File(libsPath);
               
        for (File lib : libsFolder.listFiles(JARFILTER)) {
            String arg[] = {"-z", lib.getName(), lib.getAbsolutePath()};
            main.main(arg); 
        }
        
        mt.printSetup();
    }
}

