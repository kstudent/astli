package astli.extraction;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import astli.pojo.ASTLIOptions;
import astli.pojo.PackageHierarchy;
import java.util.Map;
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
public class FeatureExtractor {
    
    private final ASTLIOptions options;
    
    private static final Logger LOGGER = LogManager.getLogger();

    public FeatureExtractor(ASTLIOptions options) {
        this.options = options;
    }
    
    public Stream<PackageHierarchy> extractPackageHierarchies() throws IOException {
        baksmaliOptions bsOptions = new baksmaliOptions();
        
        List<? extends ClassDef> classDefs = parseClassesFromFile(options.inputFileName, bsOptions);
        MethodASTBuilderFactory methodASTfactory = new MethodASTBuilderFactory(bsOptions);
        
        Map<String, String> mapping = new MappingHandler(options).getMapping();
        
        PackageHierarchyGenerator phGen = new PackageHierarchyGenerator(
                new ASTToFingerprintTransformer(), mapping);
        
        Stream<ClassASTBuilder> builderStream = classDefs.parallelStream()
                .map(classDef -> new ClassASTBuilder(classDef, methodASTfactory));
        
        return phGen.generatePackageHierarchiesFromClassBuilders(builderStream);
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
