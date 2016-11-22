package org.androidlibid.proto.ast;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import org.androidlibid.proto.ASTLIOptions;
import org.androidlibid.proto.pojo.PackageHierarchy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jf.baksmali.baksmali;
import org.jf.baksmali.baksmaliOptions;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.ClassDef;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class BaksmaliSetup {
    
    private final ASTLIOptions options;
    
    private static final Logger LOGGER = LogManager.getLogger();

    public BaksmaliSetup(ASTLIOptions options) {
        this.options = options;
    }
    
    public Stream<PackageHierarchy> setup() throws IOException {
        baksmaliOptions bsOptions = new baksmaliOptions();
        List<? extends ClassDef> classDefs = parseClassesFromFile(options.inputFileName, bsOptions);
        ASTBuilderFactory astBuilderFactory = new ASTBuilderFactory(bsOptions);
        PackageHierarchyStreamGenerator psGen = new PackageHierarchyStreamGenerator(options, classDefs, astBuilderFactory);
        return psGen.generateStream();
    }
    
    private List<? extends ClassDef> parseClassesFromFile(String fileName, baksmaliOptions bsOptions) throws IOException {
        File dexFileFile = loadDexFile(fileName);
        DexBackedDexFile dexFile = baksmali.readInAndParseTheDexFile(dexFileFile, bsOptions);
        return baksmali.disassembleDexFile(dexFile, bsOptions);
    }
            
    private File loadDexFile(String fileName) throws IOException {
        File dexFileFile = new File(fileName);
        if (!dexFileFile.exists()) {
            throw new IOException("Can't find the file " + fileName);
        }
        
        if (fileName.endsWith(".jar")) {
            String outputDexFileName = fileName + ".dex";
            dexFileFile = new File(outputDexFileName);
            
            if(dexFileFile.exists()) {
                LOGGER.warn(fileName + " has been dexed already.");
            } else {
                String[] dxArgs = {"--dex", "--core-library", "--output=" + outputDexFileName, fileName};
                com.android.dx.command.Main.main(dxArgs);
            }
        }
        
        return dexFileFile;
    }
            
    
}
